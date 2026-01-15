
import { Dropdown } from "primereact/dropdown";
import { InputText } from "primereact/inputtext";

function GiftFilter({ filter, setFilter, nameFilter, setNameFilter }) {
    const filterOptions = [
        { label: 'Show All', value: 'show all' },
        { label: 'Waiting', value: 'waiting' },
        { label: 'En Route', value: 'en route' },
        { label: 'Delivered', value: 'delivered' },
    ];

    return (
        <div className="filter-row">
            <span className="filter-delivery">
                <p>Filter by delivery status: </p>
                <Dropdown
                    className="dropdown-filter"
                    id="deliveryFilter"
                    options={filterOptions}
                    value={filter}
                    onChange={(e) => setFilter(e.value)}
                />
            </span>
            <p>Filter by name: </p>
            <span className="filter-name p-float-label">
                <InputText
                    id="Name"
                    name="Name"
                    value={nameFilter}
                    onChange={(e) => setNameFilter(e.target.value)}
                />
                <label htmlFor="Name">Name</label>
            </span>
        </div>
    );
}

export default GiftFilter;