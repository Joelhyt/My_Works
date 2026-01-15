import { useEffect, useState } from 'react'
import { Dialog } from 'primereact/dialog'
import { Button } from 'primereact/button'
import { InputText } from 'primereact/inputtext'
import { Dropdown } from 'primereact/dropdown'

function AddGiftDialog({ onAdd }) {
  const [visible, setVisible] = useState(false)
  const [form, setForm] = useState({
    name: '',
    location: '',
    giftName: '',
    priority: null,
    productionStatus: null,
    deliveryStatus: 'Waiting',
  })

  const priorityOptions = ['Low', 'Medium', 'High']

  const statusOptions = [
    { label: 'Not started', value: 'Not started', disabled: form.deliveryStatus !== 'Waiting' },
    { label: 'In progress', value: 'In progress', disabled: form.deliveryStatus !== 'Waiting' },
    { label: 'Completed', value: 'Completed', disabled: false },
  ]
  
  const deliveryStatus = [
    { label: 'Waiting', value: 'Waiting', disabled: false },
    { label: 'En Route', value: 'En Route', disabled: form.productionStatus !== 'Completed' },
    { label: 'Delivered', value: 'Delivered', disabled: form.productionStatus !== 'Completed' },
  ]

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleSubmit = () => {
    if (!form.name || !form.location || !form.giftName || !form.priority || !form.productionStatus || !form.deliveryStatus) {
      alert('Please fill in all fields.')
      return
    }
    onAdd(form)
    setForm({
      name: '',
      location: '',
      giftName: '',
      priority: null,
      productionStatus: null,
      deliveryStatus: 'Waiting',
    })
    setVisible(false)
  }

  const resetAndClose = () => {
    setForm({
      name: '',
      location: '',
      giftName: '',
      priority: null,
      productionStatus: null,
      deliveryStatus: 'Waiting',
    })
    setVisible(false)
  }

  const footer = (
    <div>
      <Button label="Cancel" outlined onClick={resetAndClose} />
      <Button label="Add Gift" onClick={handleSubmit} />
    </div>
  )

  return (
    <div>
      <Button className='header-button' label="Add Gift" onClick={() => setVisible(true)} />

      <Dialog
        header="Add Gift"
        visible={visible}
        blockScroll={true}
        modal
        className="custom-gift-dialog"
        style={{ width: '30rem', backgroundColor: '#2c2c2c' }}
        footer={footer}
        closable={false}
        onHide={() => setVisible(false)}
      >
        <div className="p-fluid" style={{ display: 'flex', flexDirection: 'column', gap: '2rem'}}>
          <span className="p-float-label">
            <InputText id="name" name="name" value={form.name} onChange={handleChange} />
            <label htmlFor="name">Name</label>
          </span>

          <span className="p-float-label">
            <InputText id="location" name="location" value={form.location} onChange={handleChange} />
            <label htmlFor="location">Location</label>
          </span>

          <span className="p-float-label">
            <InputText id="giftName" name="giftName" value={form.giftName} onChange={handleChange} />
            <label htmlFor="giftName">Gift Name</label>
          </span>

          <span className="p-float-label">
            <Dropdown
              id="priority"
              value={form.priority}
              options={priorityOptions}
              onChange={(e) => setForm({ ...form, priority: e.value })}
            />
            <label htmlFor="priority">Priority</label>
          </span>

          <span className="p-float-label">
            <Dropdown
              id="productionStatus"
              value={form.productionStatus}
              options={statusOptions}
              optionLabel="label"
              optionValue="value"
              onChange={(e) =>
                setForm({ ...form, productionStatus: e.value })
              }
              disabled={form.deliveryStatus === 'En Route' && form.deliveryStatus === 'Delivered'}
            />
            
            <label htmlFor="productionStatus">Production Status</label>
          </span>

          <span className="p-float-label">
            <Dropdown
              id="deliveryStatus"
              value={form.deliveryStatus}
              options={deliveryStatus}
              optionLabel="label"
              optionValue="value"
              onChange={(e) =>
                setForm({ ...form, deliveryStatus: e.value })
              }
              disabled={form.productionStatus !== 'Completed' && (form.deliveryStatus === 'En Route' || form.deliveryStatus === 'Delivered')}
            />
            <label htmlFor="deliveryStatus">Delivery Status</label>
          </span>
        </div>
      </Dialog>
    </div>
  )
}

export default AddGiftDialog
