import { useState } from "react";
import vector3 from "./vector-3.svg";
import vector4 from "./vector-4.svg";
import vector5 from "./vector-5.svg";
import vector6 from "./vector-6.svg";
import vector7 from "./vector-7.svg";
import vector8 from "./vector-8.svg";
import vector9 from "./vector-9.svg";
import vector10 from "./vector-10.svg";
import vector11 from "./vector-11.svg";
import vector12 from "./vector-12.svg";

type NavigationItem = {
  label: string;
  icon: string;
  iconClassName: string;
};

const navigationItems: NavigationItem[] = [
  {
    label: "Inicio",
    icon: vector3,
    iconClassName: "absolute w-[91.10%] h-[95.27%] top-[4.73%] left-[8.90%]",
  },
  {
    label: "Ventas",
    icon: vector4,
    iconClassName: "absolute w-[95.06%] h-[95.06%] top-[4.94%] left-[4.94%]",
  },
  {
    label: "Productos",
    icon: vector5,
    iconClassName: "absolute w-[91.10%] h-[95.26%] top-[4.74%] left-[8.90%]",
  },
  {
    label: "Regalos",
    icon: vector6,
    iconClassName: "absolute w-[91.10%] h-[95.27%] top-[4.73%] left-[8.90%]",
  },
  {
    label: "Ofertas",
    icon: vector7,
    iconClassName: "absolute w-[95.27%] h-[95.27%] top-[4.73%] left-[4.73%]",
  },
  {
    label: "Inventario",
    icon: vector8,
    iconClassName: "absolute w-[86.93%] h-[95.27%] top-[4.73%] left-[13.07%]",
  },
  {
    label: "Clientes",
    icon: vector9,
    iconClassName: "absolute w-[86.93%] h-[91.10%] top-[8.90%] left-[13.07%]",
  },
  {
    label: "Proveedores",
    icon: vector10,
    iconClassName: "absolute w-[91.10%] h-[95.27%] top-[4.73%] left-[8.90%]",
  },
  {
    label: "Reportes",
    icon: vector11,
    iconClassName: "absolute w-[82.77%] h-[91.10%] top-[8.90%] left-[17.23%]",
  },
  {
    label: "ConfiguraciÃ³n",
    icon: vector12,
    iconClassName: "absolute w-[90.92%] h-[95.19%] top-[4.81%] left-[9.08%]",
  },
];

export const MainNavigationSection = (): JSX.Element => {
  const [activeItem, setActiveItem] = useState("Inicio");

  return (
    <nav
      aria-label="NavegaciÃ³n principal"
      className="flex flex-col w-60 items-start gap-1.5 p-3.5 relative self-stretch bg-[linear-gradient(0deg,rgba(118,80,217,1)_0%,rgba(90,88,213,1)_100%)]"
    >
      <ul className="flex flex-col w-full items-start gap-1.5 p-0 m-0 list-none">
        {navigationItems.map((item) => {
          const isActive = activeItem === item.label;

          return (
            <li key={item.label} className="w-full">
              <button
                type="button"
                aria-current={isActive ? "page" : undefined}
                onClick={() => setActiveItem(item.label)}
                className={`flex h-[49px] items-center gap-[18px] px-4 py-0 relative self-stretch w-full rounded-lg overflow-hidden border-0 cursor-pointer text-left ${
                  isActive ? "bg-[#cf3acddd]" : "bg-transparent"
                }`}
              >
                <span
                  aria-hidden="true"
                  className="relative w-[25px] h-[25px] shrink-0"
                >
                  <img className={item.iconClassName} alt="" src={item.icon} />
                </span>
                <span className="w-fit text-white text-[15px] leading-[18.8px] whitespace-nowrap relative [font-family:'Inter-Regular',Helvetica] font-normal tracking-[0]">
                  {item.label}
                </span>
              </button>
            </li>
          );
        })}
      </ul>
    </nav>
  );
};

