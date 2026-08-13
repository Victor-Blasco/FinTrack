import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'
import { jwtVerify, base64url } from 'jose'

const RAW_PRIMARY_SECRET = process.env.JWT_SECRET || 'dGhpcy1pcy1hLXZlcnktc2VjdXJlLXNlY3JldC1rZXktZm9yLWp3dC1zaWduaW5nLXF1YW50dW0tZnJhdWQtc2hpZWxk'
const PRIMARY_SECRET_BYTES = new TextEncoder().encode(RAW_PRIMARY_SECRET)

let PRIMARY_BASE64_BYTES: Uint8Array | null = null
try {
  PRIMARY_BASE64_BYTES = base64url.decode(RAW_PRIMARY_SECRET)
} catch {
  // Ignorar si no es Base64
}

const FALLBACK_SECRET_BYTES = new TextEncoder().encode('defaultSecretKeyThatIsAtLeast32BytesLongForHmacSha256!')

const SERVICE_ROUTES = [
  { prefix: '/api/v1/auth', targetUrl: 'http://localhost:8081' },
  { prefix: '/api/v1/ingest', targetUrl: 'http://localhost:8082' },
  { prefix: '/api/v1/accounts', targetUrl: 'http://localhost:8083' },
  { prefix: '/api/v1/budgets', targetUrl: 'http://localhost:8083' },
  { prefix: '/api/v1/transactions', targetUrl: 'http://localhost:8083' },
]

async function getVerifiedUserId(token: string): Promise<string | null> {
  try {
    const { payload } = await jwtVerify(token, PRIMARY_SECRET_BYTES)
    return typeof payload.sub === 'string' ? payload.sub : null
  } catch {
    if (PRIMARY_BASE64_BYTES) {
      try {
        const { payload } = await jwtVerify(token, PRIMARY_BASE64_BYTES)
        return typeof payload.sub === 'string' ? payload.sub : null
      } catch {}
    }
    try {
      const { payload } = await jwtVerify(token, FALLBACK_SECRET_BYTES)
      return typeof payload.sub === 'string' ? payload.sub : null
    } catch {
      return null
    }
  }
}

/**
 * Extrae el token prioritariamente de las Cookies HttpOnly y, si no existe, de la cabecera Authorization.
 */
function extractToken(request: NextRequest): string | null {
  // 1. Prioridad: Cookie HttpOnly
  const cookieToken = request.cookies.get('accessToken')?.value
  if (cookieToken && cookieToken.trim()) {
    return cookieToken.trim().replace(/^["']|["']$/g, '')
  }

  // 2. Fallback: Cabecera Authorization
  const authHeader = request.headers.get('authorization') || request.headers.get('Authorization')
  if (authHeader && authHeader.trim()) {
    let token = authHeader.trim()
    if (token.toLowerCase().startsWith('bearer ')) {
      token = token.substring(7).trim()
    }
    return token.replace(/^["']|["']$/g, '')
  }

  return null
}

export async function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl

  // Dejar pasar peticiones públicas de auth manejadas por Next.js Route Handlers o auth-service directo
  if (pathname.startsWith('/api/v1/auth/login') || pathname.startsWith('/api/v1/auth/register') || pathname.startsWith('/api/v1/auth/logout')) {
    return NextResponse.next()
  }

  const matchedRoute = SERVICE_ROUTES.find(
    (route) => route.prefix !== '/api/v1/auth' && pathname.startsWith(route.prefix)
  )

  if (!matchedRoute) {
    return NextResponse.next()
  }

  // Extracción limpia desde Cookie / Header
  const rawToken = extractToken(request)
  if (!rawToken) {
    return NextResponse.json(
      { success: false, message: 'No autenticado. Sesión no encontrada.' },
      { status: 401 }
    )
  }

  const userId = await getVerifiedUserId(rawToken)

  if (!userId) {
    return NextResponse.json(
      { success: false, message: 'Token de acceso inválido o expirado' },
      { status: 401 }
    )
  }

  // Inyectar cabeceras para el microservicio backend
  const requestHeaders = new Headers(request.headers)
  requestHeaders.set('Authorization', `Bearer ${rawToken}`)
  requestHeaders.set('X-User-Id', userId)

  const targetUrl = new URL(pathname, matchedRoute.targetUrl)

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
  } catch (targetErr) {
    return NextResponse.json(
      { success: false, message: `Microservicio en ${matchedRoute.targetUrl} no disponible` },
      { status: 503 }
    )
  }
}

export default proxy

export const config = {
  matcher: ['/api/v1/:path*'],
}
