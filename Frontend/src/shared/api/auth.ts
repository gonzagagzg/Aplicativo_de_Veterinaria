import { useMutation } from '@tanstack/react-query'
import { api } from './client'
import type { LoginRequest, LoginResponse } from '@/shared/types/api'

/** POST /api/auth/login — único endpoint público, sin Authorization. */
export function useLogin() {
  return useMutation<LoginResponse, Error, LoginRequest>({
    mutationFn: (credenciales) => api.post<LoginResponse>('/api/auth/login', credenciales),
  })
}
