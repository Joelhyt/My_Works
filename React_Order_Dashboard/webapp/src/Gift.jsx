
import EditGiftDialog from './EditGiftDialog.jsx'

function Gift({ gift, onDelete, onEdit, usePriorityColors }) {
  

  function handleDelete() {
      onDelete(gift.id)
  }

  const priorityClass = usePriorityColors && gift.priority
    ? `gift-card priority-${gift.priority.toLowerCase()}`
    : 'gift-card'

  return (
    <div className={`gift-container ${priorityClass}`}>
      <p className='gift-id'><strong>ID:</strong> {gift.id}</p>
      <p><strong>Name:</strong> {gift.name}</p>
      <p><strong>Location:</strong> {gift.location}</p>
      <p><strong>Gift Name:</strong> {gift.giftName}</p>
      <p><strong>Priority:</strong> {gift.priority}</p>
      <p><strong>Production Status:</strong> {gift.productionStatus}</p>
      <p><strong>Delivery Status:</strong> {gift.deliveryStatus}</p>
        <div className="button-group">
          <EditGiftDialog onEdit={onEdit} gift={gift} />
          <button className="buttons" onClick={handleDelete}>Delete</button>
        </div>
    </div>     
  )
}

export default Gift
