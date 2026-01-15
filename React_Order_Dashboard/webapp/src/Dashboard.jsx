function Dashboard({ gifts = [] }) {
  const total = gifts.length;
  const notStarted = gifts.filter(g => g.productionStatus === 'Not started').length;
  const inProgress = gifts.filter(g => g.productionStatus === 'In progress').length;
  const completed = gifts.filter(g => g.productionStatus === 'Completed').length;
  const enRoute = gifts.filter(g => g.deliveryStatus === 'En Route').length;
  const delivered = gifts.filter(g => g.deliveryStatus === 'Delivered').length;
  const completedOrDelivered = gifts.filter(
    g => g.productionStatus === 'Completed' || g.deliveryStatus === 'Delivered'
  ).length;

  const ratio = total > 0 ? ((completedOrDelivered / total) * 100).toFixed(1) : 0;

  return (
    <div className="dashboard">
      <h1>Overview</h1>

      <div className="dashboard-grid">
        <div className="stat">
          <span>Total gifts</span>
          <strong>{total}</strong>
        </div>
        <div className="stat">
          <span>Not started</span>
          <strong>{notStarted}</strong>
        </div>
        <div className="stat">
          <span>In progress</span>
          <strong>{inProgress}</strong>
        </div>
        <div className="stat">
          <span>Completed</span>
          <strong>{completed}</strong>
        </div>
        <div className="stat">
          <span>En route</span>
          <strong>{enRoute}</strong>
        </div>
        <div className="stat">
          <span>Delivered</span>
          <strong>{delivered}</strong>
        </div>
      </div>

      <div className="progress-wrapper">
        <div className="progress-label">
          Completion: {completedOrDelivered} / {total} ({ratio}%)
        </div>
        <div className="progress-bar">
          <div
            className="progress-fill"
            style={{ width: `${ratio}%` }}
          />
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
