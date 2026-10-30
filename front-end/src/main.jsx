import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
// Bootstrap foi removido: o projeto não usa nenhum componente da lib (sem
// .container, .row, .form-control etc.), só o reset. Esse reset definia
// .btn-primary/.btn-danger/.btn-ghost em AZUL, com o MESMO nome das nossas
// classes — por isso os botões ficavam azuis ao clicar (:active/:focus).
import './index.css'
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
