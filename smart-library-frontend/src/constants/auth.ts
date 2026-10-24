export const ADMIN_ROLE = 'ADMIN'
export const ADMIN_ACCOUNT = 'admin'

export const isAdminUser = (userInfo: any) => {
  return userInfo?.role === ADMIN_ROLE
}
