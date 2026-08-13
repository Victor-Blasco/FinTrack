import { NextResponse } from 'next/server'
import { cookies } from 'next/headers'

/**
 * Route Handler BFF de cierre de sesión.
 * Elimina la cookie HttpOnly accessToken.
 */
export async function POST() {
  const cookieStore = await cookies()
  cookieStore.delete('accessToken')
  return NextResponse.json({ success: true })
}
