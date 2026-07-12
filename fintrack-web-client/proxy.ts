import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

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
      const validateRes = await fetch('http://localhost:8081/api/v1/auth/validate', {
        headers: {
          'Authorization': authHeader
        }
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

      // Clonar cabeceras y añadir X-User-Id
      const requestHeaders = new Headers(request.headers)
      requestHeaders.set('X-User-Id', userId)

      // Identificar el microservicio destino
      let targetServiceUrl = ''
      if (pathname.startsWith('/api/v1/accounts') || pathname.startsWith('/api/v1/budgets')) {
        targetServiceUrl = 'http://localhost:8083' // finance-profile-service
      } else if (pathname.startsWith('/api/v1/ingest')) {
        targetServiceUrl = 'http://localhost:8082' // banking-ingest-service
      }

      const targetUrl = new URL(pathname, targetServiceUrl)
      
      // Reescribir (Reverse Proxy) con cabeceras modificadas
      return NextResponse.rewrite(targetUrl, {
        request: {
          headers: requestHeaders,
        }
      })

    } catch (error) {
      console.error('Error validating token in proxy:', error)
      return NextResponse.json(
        { success: false, message: 'Internal validation error' },
        { status: 500 }
      )
    }
  }

  return NextResponse.next()
}

// Interceptar todas las rutas de API
export const config = {
  matcher: ['/api/v1/:path*'],
}
