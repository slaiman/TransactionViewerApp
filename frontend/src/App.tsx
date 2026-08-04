import { useState } from "react";
import { NavTabs, type View } from "./components/NavTabs";
import { TransactionsPage } from "./pages/TransactionsPage";
import { DashboardPage } from "./pages/DashboardPage";

function App() {
  const [activeView, setActiveView] = useState<View>("transactions");

  return (
    <div className="min-h-screen bg-paper">
      <div className="mx-auto max-w-4xl px-6 py-10">
        <NavTabs active={activeView} onChange={setActiveView} />
        {activeView === "transactions" ? <TransactionsPage /> : <DashboardPage />}
      </div>
    </div>
  );
}

export default App;
