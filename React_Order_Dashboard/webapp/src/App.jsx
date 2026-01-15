import { useEffect, useState, useRef } from 'react'
import { v4 as uuidv4 } from 'uuid'
import './App.css'
import './AddGiftDialog.css'
import './Gift.css'
import './GiftGrid.css'
import './Dashboard.css'
import './GiftFilter.css'
import GiftGrid from './GiftGrid.jsx'
import AddGiftDialog from './AddGiftDialog'
import Dashboard from './Dashboard.jsx'
import GiftFilter from './GiftFilter.jsx'
import Header from './Header.jsx'

function App() {

const LOCAL_STORAGE_KEY = 'giftsData';
const LOCAL_STORAGE_KEY_2 = 'usePriorityColors';

  const [gifts, setGifts] = useState([])
  const [usePriorityColors, setUsePriorityColors] = useState(true)
  const [filter, setFilter] = useState('show all');
  const [nameFilter, setNameFilter] = useState('');
  const [snowfall, setSnowfall] = useState(true);
  const firstLoad = useRef(true);
  const firstLoad_2 = useRef(true);

  useEffect(() => {
    const storedUsePriorityColors = JSON.parse(localStorage.getItem(LOCAL_STORAGE_KEY_2));
    if (storedUsePriorityColors !== null) {
      setUsePriorityColors(storedUsePriorityColors);
    }
  }, []);

  useEffect(() => {
    if (firstLoad_2.current) {
      firstLoad_2.current = false;
      return;
    }
    localStorage.setItem(LOCAL_STORAGE_KEY_2, JSON.stringify(usePriorityColors));
  }, [usePriorityColors]);

  useEffect(() => {
    const storedGifts = JSON.parse(localStorage.getItem(LOCAL_STORAGE_KEY));
    if (storedGifts) {
      setGifts(storedGifts);
    }
  }, []);

  useEffect(() => {
    if (firstLoad.current) {
      firstLoad.current = false;
      return;
    }
    localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(gifts));
  }, [gifts]);


  // ---------------- Mock gifts to add ----------------

  const mockGifts = [
    {
      id: uuidv4(),
      name: 'Make',
      location: 'Perniö',
      giftName: 'Leluauto',
      priority: 'High',
      productionStatus: 'In progress',
      deliveryStatus: 'Waiting'
    },
    {
      id: uuidv4(),
      name: 'Paavo',
      location: 'Hämeenlinna',
      giftName: 'Pelitietokone',
      priority: 'Medium',
      productionStatus: 'Completed',
      deliveryStatus: 'En Route'
    },
    {
      id: uuidv4(),
      name: 'Liisa',
      location: 'Rovaniemi',
      giftName: 'Vauvanukke',
      priority: 'Low',
      productionStatus: 'Completed',
      deliveryStatus: 'Delivered'
    },
  ];

  const addMockGifts = () => {
      setGifts(mockGifts)
  }

  const handleAddGift = (gift) => {
    setGifts((prev) => [...prev, { ...gift, id: uuidv4() }])
  }

  const handleDeleteGift = (id) => {
    setGifts((prev) => prev.filter((g) => g.id !== id))
  }

  const handleEditGift = (updatedGift) => {
    setGifts((prev) =>
      prev.map((g) => (g.id === updatedGift.id ? updatedGift : g))
    )
  }

  const filteredGifts = gifts.filter((gift) => {

    const nameMatch = nameFilter === '' || (gift.name && gift.name.toLowerCase().includes(nameFilter.toLowerCase()));

    if (filter === 'show all') return nameMatch;
    if (filter === 'waiting') return nameMatch && gift.deliveryStatus === 'Waiting';
    if (filter === 'en route') return nameMatch && gift.deliveryStatus === 'En Route';
    if (filter === 'delivered') return nameMatch && gift.deliveryStatus === 'Delivered';
    return nameMatch;
  });

  return (
    <>
      <Header 
        snowfall={snowfall} 
        setSnowfall={setSnowfall} 
        setUsePriorityColors={setUsePriorityColors} 
      />

      <AddGiftDialog onAdd={handleAddGift} />
      <div className='lower-area'>
        <GiftFilter
          filter={filter}
          setFilter={setFilter}
          nameFilter={nameFilter}
          setNameFilter={setNameFilter}
        />
        <div className='grid-dash-row'>
          <GiftGrid
            gifts={filteredGifts}
            onDelete={handleDeleteGift}
            onEdit={handleEditGift}
            usePriorityColors={usePriorityColors}
            addMockGifts={addMockGifts}
            filter={filter}
            nameFilter={nameFilter}
          />
          <Dashboard gifts={gifts} />
        </div>
      </div>
    </>
  )
}

export default App
