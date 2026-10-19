import icon from "./assets/cherubini.svg";

export default function App() {
  return (
    <main className="page">
      <section className="card" aria-labelledby="title">
        <img className="icon" src={icon} alt="" width="88" height="88" />
        <p className="badge">Lavori in corso</p>
        <h1 id="title">Vocabolario milanese–italiano</h1>
        <p className="subtitle">Francesco Cherubini · voci tecniche</p>
        <p className="message">
          L'edizione digitale online è in preparazione. Torna a trovarci presto
          per consultare il vocabolario, cercare le parole nelle definizioni e
          stampare le voci che ti interessano.
        </p>
      </section>
      <footer className="footer">
        A cura di Stefano Pilato e Khova Krishna Pilato
      </footer>
    </main>
  );
}
