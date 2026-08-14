import { NextResponse } from 'next/server'

/**
 * Route Handler BFF de registro de usuario.
 * Reenvía la petición de registro a auth-service (8081).
 */
export async function POST(request: Request) {
  try {
    const body = await request.json()

    const backendRes = await fetch('http://localhost:8081/api/v1/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })

    const data = await backendRes.json()

    if (!backendRes.ok) {
      return NextResponse.json(
        { success: false, message: data.message || 'Error en el registro' },
        { status: backendRes.status }
      )
    }

    return NextResponse.json(data, { status: 201 })
  } catch (error) {
    console.error('[BFF Register Error]', error)
    return NextResponse.json(
      { success: false, message: 'Error en la pasarela de registro' },
      { status: 500 }
    )
  }
}
