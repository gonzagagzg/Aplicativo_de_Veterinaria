/**
 * Validación de cédula ecuatoriana (algoritmo módulo 10 del SRI).
 *
 * Réplica en cliente de `EcuadorValidator.cedulaValida` (backend) para dar
 * feedback inmediato en el formulario. El backend vuelve a validar en cada
 * POST/PUT — esto es solo UX, no la fuente de verdad.
 */
export function cedulaEcuatorianaValida(valor: string): boolean {
  const limpio = valor.replace(/\D/g, '')
  if (limpio.length !== 10) return false

  const provincia = Number(limpio.slice(0, 2))
  if (!((provincia >= 1 && provincia <= 24) || provincia === 30)) return false

  const tercerDigito = Number(limpio[2])
  if (tercerDigito > 5) return false

  const coeficientes = [2, 1, 2, 1, 2, 1, 2, 1, 2]
  let suma = 0
  for (let i = 0; i < 9; i++) {
    let valorPos = Number(limpio[i]) * coeficientes[i]
    if (valorPos > 9) valorPos -= 9
    suma += valorPos
  }
  const digitoVerificador = (10 - (suma % 10)) % 10
  return digitoVerificador === Number(limpio[9])
}
