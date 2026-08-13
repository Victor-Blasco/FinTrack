import { NextResponse } from 'next/server'
import { cookies } from 'next/headers'

/**
 * Route Handler BFF de inicio de sesión.
 * Intercepta las credenciales, consulta a auth-service (8081) y establece la cookie HttpOnly.
 */
export async function POST(request: Request) {
  try {
    const body = await request.json()

    const backendRes = await fetch('http://localhost:8081/api/v1/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })

    const data = await backendRes.json()

    if (!backendRes.ok) {
      return NextResponse.json(
        { success: false, message: data.message || 'Credenciales incorrectas' },
        { status: backendRes.status }
      )
    }

    const token = data.token || data.accessToken

    if (!token) {
      return NextResponse.json({ success: false, message: 'Respuesta de autenticación inválida' }, { status: 500 })
    }

    // Establecer la cookie HttpOnly segura
    const cookieStore = await cookies()
    cookieStore.set('accessToken', token, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'lax',
      path: '/',
      maxAge: 60 * 60 * 24, // 1 día
    })

    return NextResponse.json({ success: true, user: data.user || data.email })
  } catch (error) {
    console.error('[BFF Auth Error]', error)
    return NextResponse.json({ success: false, message: 'Error en la pasarela de autenticación' }, { status: 500 })
  }
}
