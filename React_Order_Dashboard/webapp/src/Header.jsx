function Header({ snowfall, setSnowfall, setUsePriorityColors }) {

    return (
        <header className="header">
            { snowfall && <div class="snowfall">
            <div class="snowflake"></div>
            <div class="snowflake"></div>
            <div class="snowflake"></div>
            <div class="snowflake"></div>
            <div class="snowflake"></div>
            <div class="snowflake"></div>
            <div class="snowflake"></div>
            <div class="snowflake"></div>
            <div class="snowflake"></div>
            <div class="snowflake"></div>
            <div class="snowflake"></div>
            <div class="snowflake"></div>
            <div class="snowflake"></div>
            </div> }
            <h1>Gift delivery control center</h1>
            <button className="header-button" onClick={() => setUsePriorityColors((p) => !p)}>
            Toggle Priority Colors
            </button>
            <button className="header-button" onClick={() => setSnowfall((s) => !s)}>Toggle Snowfall</button>
        </header>
    );

}

export default Header;