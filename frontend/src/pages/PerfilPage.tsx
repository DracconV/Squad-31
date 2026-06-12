import { useAuth } from '../contexts/AuthContext'

export default function PerfilPage() {
  const { user } = useAuth()
  return (
    <div className="max-w-md">
      <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
        <div className="flex items-center gap-4 mb-6">
          <div className="w-14 h-14 rounded-full bg-blue-600 flex items-center justify-center text-white text-xl font-bold">
            {user?.nome?.charAt(0).toUpperCase() ?? 'U'}
          </div>
          <div>
            <p className="font-semibold text-gray-800">{user?.nome}</p>
            <p className="text-sm text-gray-500">{user?.perfil}</p>
          </div>
        </div>
        <div className="space-y-3 text-sm">
          <div className="flex justify-between border-b border-gray-50 pb-2">
            <span className="text-gray-500">ID</span>
            <span className="text-gray-700 font-mono text-xs">{user?.id}</span>
          </div>
          <div className="flex justify-between border-b border-gray-50 pb-2">
            <span className="text-gray-500">Perfil</span>
            <span className="text-gray-700">{user?.perfil}</span>
          </div>
        </div>
      </div>
    </div>
  )
}
