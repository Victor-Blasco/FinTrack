import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

/**
 * Middleware de la API Gateway / Backend-For-Frontend (BFF) en Next.js.
 * <p>
 * Se encarga de interceptar y enrutar las peticiones entrantes bajo `/api/v1/`:
 * 1. Redirecciona las rutas de autenticación pública (`/register`, `/login`) hacia `auth-service` (Puerto 8081).
 * 2. Valida los tokens Bearer JWT de los endpoints protegidos consultando a `auth-service`.
 * 3. Inyecta la cabecera `X-User-Id` con la identidad verificada del usuario.
 * 4. Realiza la reescritura de proxy inverso (Reverse Proxy) hacia los microservicios destino:
 *    - `/api/v1/accounts` y `/api/v1/budgets` -> `finance-profile-service` (Puerto 8083).
 *    - `/api/v1/ingest` -> `banking-ingest-service` (Puerto 8082).
 * </p>
 *
 * @param request petición HTTP entrante {@link NextRequest}
 * @returns {@link NextResponse} con reescritura de proxy, respuesta JSON de error 401/500 o paso directo
 */
export async function proxy(request: NextRequest) {
  const url = request.nextUrl.clone()
  const { pathname } = url

  // 1. Redireccionar solicitudes públicas de autenticación al puerto 8081
  if (pathname.startsWith('/api/v1/auth/register') || pathname.startsWith('/api/v1/auth/login')) {
    const targetUrl = new URL(pathname, 'http://localhost:8081')
    return NextResponse.rewrite(targetUrl)
  }

  // 2. Validar token para los endpoints protegidos
  if (
    pathname.startsWith('/api/v1/accounts') ||
    pathname.startsWith('/api/v1/budgets') ||
    pathname.startsWith('/api/v1/transactions') ||
    pathname.startsWith('/api/v1/ingest')
  ) {
    const authHeader = request.headers.get('authorization')
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return NextResponse.json(
        { success: false, message: 'Missing or invalid Authorization header' },
        { status: 401 }
      )
    }

    try {
      // Validar token llamando al endpoint interno de auth-service
      const rawToken = authHeader.replace(/^Bearer\s+/, '')
      const validateRes = await fetch(`http://localhost:8081/api/v1/auth/validate?token=${encodeURIComponent(rawToken)}`, {
        method: 'POST'
      })

      if (!validateRes.ok) {
        return NextResponse.json(
          { success: false, message: 'Invalid or expired token' },
          { status: 401 }
        )
      }

      const valData = await validateRes.json()
      const userId = valData.userId

      if (!userId) {
        return NextResponse.json(
          { success: false, message: 'User ID missing in validation response' },
          { status: 401 }
        )
      }

      // Clonar cabeceras, eliminar X-User-Id malicioso enviado por el cliente e inyectar el verificado
      const requestHeaders = new Headers(request.headers)
      requestHeaders.delete('x-user-id')
      requestHeaders.set('X-User-Id', userId)

      // Identificar el microservicio destino
      let targetServiceUrl = ''
      if (
        pathname.startsWith('/api/v1/accounts') ||
        pathname.startsWith('/api/v1/budgets') ||
        pathname.startsWith('/api/v1/transactions')
      ) {
        targetServiceUrl = 'http://localhost:8083' // finance-profile-service
      } else if (pathname.startsWith('/api/v1/ingest')) {
        targetServiceUrl = 'http://localhost:8082' // banking-ingest-service
      }

      const targetUrl = new URL(pathname, targetServiceUrl)
      
      // Realizar la petición HTTP desde el servidor Node para capturar la desconexión limpiamente
      try {
        const body = ['GET', 'HEAD'].includes(request.method) ? undefined : await request.blob()
        const backendRes = await fetch(targetUrl.toString(), {
          method: request.method,
          headers: requestHeaders,
          body,
        })

        const responseData = await backendRes.blob()
        const responseHeaders = new Headers(backendRes.headers)
        responseHeaders.delete('www-authenticate')

        return new NextResponse(responseData, {
          status: backendRes.status,
          statusText: backendRes.statusText,
          headers: responseHeaders,
        })
      } catch (targetErr: any) {
        console.warn(`[Proxy Bypassed] El servicio ${targetServiceUrl} está apagado. Operando con sincronización local.`);
        return NextResponse.json(
          { success: false, message: `Microservicio ${targetServiceUrl} no disponible` },
          { status: 503 }
        )
      }

    } catch (error: any) {
      // Si auth-service (8081) o los microservicios están apagados, responder limpiamente sin romper el cliente
      const isConnectionRefused = error?.code === 'ECONNREFUSED' || error?.cause?.code === 'ECONNREFUSED';
      if (isConnectionRefused) {
        console.warn('Microservicio backend desconectado (ECONNREFUSED). Operando en modo cliente.');
        return NextResponse.json(
          { success: false, message: 'Servicio backend no disponible temporalmente' },
          { status: 503 }
        )
      }

      console.error('Error procesando proxy en middleware:', error)
      return NextResponse.json(
        { success: false, message: 'Error interno en la pasarela de la API' },
        { status: 500 }
      )
    }
  }

  return NextResponse.next()
}

/**
 * Configuración de patrones de ruta coincidentes para activar el middleware.
 */
export const config = {
  matcher: ['/api/v1/:path*'],
}
