import { useState } from 'react';
import './App.css';

function App() {
  const [count, setCount] = useState(0);

  return (
    <div className="app">
      <header className="app__header">
        <h1>Sistema de Gestión Inmobiliaria</h1>
        <p>Frontend setup listo. Construye la experiencia conversacional aquí.</p>
      </header>
      <main className="app__content">
        <button type="button" onClick={() => setCount((value) => value + 1)}>
          Clicks: {count}
        </button>
      </main>
    </div>
  );
}

export default App;
