import { useState } from "react";
import chatgptImage2Oct2026122304PM1 from "./assets/chatgpt-image-2-oct-2026-12-23-04-p-m-1.png";
import image from "./assets/image.svg";
import vector from "./assets/vector.svg";
import vector2 from "./vector-2.svg";

export const DashboardHeaderSection = () => {
  const [searchTerm, setSearchTerm] = useState("");
  const [isAccountMenuOpen, setIsAccountMenuOpen] = useState(false);

  const handleSearchSubmit = (event) => {
    event.preventDefault();
  };

  return (
    <header className="flex h-20 items-center gap-[90px] px-7 py-0 relative self-stretch w-full bg-[linear-gradient(90deg,rgba(207,54,217,1)_0%,rgba(134,81,222,1)_38%,rgba(83,93,240,1)_100%)]">
      <a href="/" aria-label="Novedades App inicio">
        <img
          className="relative w-[265px] h-20 aspect-[2.91] object-cover"
          alt="Novedades App"
          src={chatgptImage2Oct2026122304PM1}
        />
      </a>
      <div className="relative flex-1 grow h-px" aria-hidden="true" />
      <form
        className="flex w-[370px] h-10 items-center gap-2.5 px-3.5 py-0 relative bg-[#f7f6ffed] rounded-[10px] overflow-hidden"
        role="search"
        onSubmit={handleSearchSubmit}
      >
        <div className="relative w-[18px] h-[18px]" aria-hidden="true">
          <img
            className="absolute w-[92.50%] h-[92.50%] top-[7.50%] left-[7.50%]"
            alt=""
            src={vector}
          />
        </div>
        <label htmlFor="product-search" className="sr-only">
          Buscar producto
        </label>
        <input
          id="product-search"
          name="product-search"
          type="search"
          value={searchTerm}
          onChange={(event) => setSearchTerm(event.target.value)}
          placeholder="Buscar producto..."
          className="w-full min-w-0 bg-transparent border-0 outline-none text-[#655ac5] text-sm leading-[17.5px] placeholder:text-[#655ac5] relative [font-family:'Inter-Regular',Helvetica] font-normal tracking-[0]"
        />
      </form>
      <div className="inline-flex items-center gap-2.5 relative flex-[0_0_auto]">
        <button
          type="button"
          className="flex w-[34px] h-[34px] items-center justify-center relative bg-white rounded-[50px] overflow-hidden"
          aria-label="Abrir menÃº de cuenta"
          aria-expanded={isAccountMenuOpen}
          onClick={() => setIsAccountMenuOpen((isOpen) => !isOpen)}
        >
          <span className="relative w-6 h-6" aria-hidden="true">
            <img
              className="absolute w-[87.08%] h-[91.25%] top-[8.75%] left-[12.92%]"
              alt=""
              src={image}
            />
          </span>
        </button>
        <button
          type="button"
          className="w-fit text-white text-[15px] leading-[18.8px] whitespace-nowrap relative [font-family:'Inter-Bold',Helvetica] font-bold tracking-[0]"
          aria-expanded={isAccountMenuOpen}
          onClick={() => setIsAccountMenuOpen((isOpen) => !isOpen)}
        >
          Caja 1
        </button>
        <button
          type="button"
          className="relative w-[15px] h-[15px]"
          aria-label={
            isAccountMenuOpen ? "Cerrar menÃº de cuenta" : "Abrir menÃº de cuenta"
          }
          aria-expanded={isAccountMenuOpen}
          onClick={() => setIsAccountMenuOpen((isOpen) => !isOpen)}
        >
          <img
            className="absolute w-[81.00%] h-[68.50%] top-[31.50%] left-[19.00%]"
            alt=""
            src={vector2}
          />
        </button>
        {isAccountMenuOpen && (
          <div
            className="absolute right-0 top-12 z-10 min-w-[140px] rounded-[10px] bg-white p-2 shadow-lg"
            role="menu"
          >
            <button
              type="button"
              className="w-full rounded-md px-3 py-2 text-left text-sm text-[#655ac5] hover:bg-[#f7f6ff]"
              role="menuitem"
              onClick={() => setIsAccountMenuOpen(false)}
            >
              Mi cuenta
            </button>
            <button
              type="button"
              className="w-full rounded-md px-3 py-2 text-left text-sm text-[#655ac5] hover:bg-[#f7f6ff]"
              role="menuitem"
              onClick={() => setIsAccountMenuOpen(false)}
            >
              Cerrar sesiÃ³n
            </button>
          </div>
        )}
      </div>
    </header>
  );
};

