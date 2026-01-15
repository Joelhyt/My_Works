import Gift from './Gift'
import './GiftGrid.css'
import { ScrollPanel } from 'primereact/scrollpanel';

function GiftGrid({ gifts, onDelete, onEdit, usePriorityColors, addMockGifts, filter, nameFilter }) {
  return (
    <ScrollPanel className='scrollpanel' style={{ width: '60%', height: '90vh' }}>
      <div className="gift-grid">
        {gifts.length === 0 ? <div className='no-gifts'>
          <h1>No gifts available</h1>
          {filter === 'show all' && nameFilter === '' && <button className='add-example-button' onClick={addMockGifts}>Add example gifts</button>}
          {(filter !== 'show all' || nameFilter !== '') && <h1>with this filter</h1>}
          </div> 
          :
          gifts.map((gift) => (
            <Gift
              key={gift.id}
              gift={gift}
              onDelete={onDelete}
              onEdit={onEdit}
              usePriorityColors={usePriorityColors}
            />
         
        ))}
      </div>
    </ScrollPanel>
  )
}

export default GiftGrid
