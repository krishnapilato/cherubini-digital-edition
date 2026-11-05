import { RotateCw, Trash2 } from "lucide-react";
import { Component, type ReactNode } from "react";

interface Props {
  readonly children: ReactNode;
}

interface State {
  readonly error: Error | null;
}

const restart = () => {
  sessionStorage.clear();
  location.reload();
};

// React still needs a class component to catch rendering errors. Instead of a blank
// page the reader sees what went wrong and can start over with a clean session.
export class ErrorBoundary extends Component<Props, State> {
  override state: State = { error: null };

  static getDerivedStateFromError(error: unknown): State {
    return { error: error instanceof Error ? error : new Error(String(error)) };
  }

  override render() {
    const { error } = this.state;
    if (!error) return <>{this.props.children}</>;
    return (
      <main className="load-error" role="alert">
        <h1>Qualcosa è andato storto</h1>
        <p>{error.message}</p>
        <div className="panel-row">
          <button type="button" className="button accent" onClick={() => location.reload()}>
            <RotateCw size={17} aria-hidden="true" />
            Ricarica
          </button>
          <button type="button" className="button" onClick={restart}>
            <Trash2 size={17} aria-hidden="true" />
            Ricomincia da capo
          </button>
        </div>
      </main>
    );
  }
}
