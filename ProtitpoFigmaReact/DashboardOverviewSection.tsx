import image from "./assets/image.png";
import imagenDelProducto from "./assets/imagen-del-producto.png";
import imagenDelProducto2 from "./assets/imagen-del-producto-2.png";
import imagenDelProducto3 from "./assets/imagen-del-producto-3.png";
import imagenDelProducto4 from "./assets/imagen-del-producto-4.png";
import vector13 from "./vector-13.svg";
import vector14 from "./vector-14.svg";
import vector15 from "./vector-15.svg";
import vector16 from "./vector-16.svg";
import vector17 from "./vector-17.svg";

type Metric = {
  title: string;
  value: string;
  description?: string;
  background: string;
  icon: string;
  iconClassName: string;
};

type Sale = {
  id: string;
  date: string;
  total: string;
  payment: string;
};

type Product = {
  name: string;
  quantity: string;
  image: string;
};

const metrics: Metric[] = [
  {
    title: "Ventas del dÃ­a",
    value: "$1,245.00",
    description: "â†‘ 12% vs. ayer",
    background: "bg-[#ca38d9]",
    icon: vector13,
    iconClassName: "absolute w-[93.55%] h-[93.55%] top-[6.45%] left-[6.45%]",
  },
  {
    title: "Productos",
    value: "124",
    description: "en inventario",
    background: "bg-[#8253e9]",
    icon: vector14,
    iconClassName: "absolute w-[89.59%] h-[93.75%] top-[6.25%] left-[10.41%]",
  },
  {
    title: "Valor inventario",
    value: "$8,760.00",
    background: "bg-[#5968f4]",
    icon: vector15,
    iconClassName: "absolute w-[93.75%] h-[93.76%] top-[6.24%] left-[6.25%]",
  },
  {
    title: "Stock bajo",
    value: "8",
    description: "productos",
    background: "bg-[#a28aea]",
    icon: vector16,
    iconClassName: "absolute w-[93.83%] h-[89.65%] top-[10.35%] left-[6.17%]",
  },
];

const sales: Sale[] = [
  {
    id: "000124",
    date: "26/09/2025\u00a0\u00a010:18 a. m.",
    total: "$320.00",
    payment: "â—‰ Efectivo",
  },
  {
    id: "000123",
    date: "26/09/2025\u00a0\u00a009:45 a. m.",
    total: "$185.00",
    payment: "â—‰ Tarjeta",
  },
  {
    id: "000122",
    date: "26/09/2025\u00a0\u00a009:12 a. m.",
    total: "$240.00",
    payment: "â—‰ Efectivo",
  },
  {
    id: "000121",
    date: "26/09/2025\u00a0\u00a008:37 a. m.",
    total: "$98.00",
    payment: "â—‰ Transferencia",
  },
];

const products: Product[] = [
  {
    name: "Cuaderno espiral",
    quantity: "32",
    image: imagenDelProducto,
  },
  {
    name: "Plumas de gel",
    quantity: "28",
    image,
  },
  {
    name: "Peluches",
    quantity: "18",
    image: imagenDelProducto2,
  },
  {
    name: "Taza personalizada",
    quantity: "15",
    image: imagenDelProducto3,
  },
  {
    name: "LÃ¡pices de colores",
    quantity: "12",
    image: imagenDelProducto4,
  },
];

export const DashboardOverviewSection = (): JSX.Element => {
  return (
    <main className="flex-col gap-4 p-6 flex-1 grow flex items-start relative self-stretch">
      <header className="flex items-start justify-between relative self-stretch w-full flex-[0_0_auto]">
        <div className="inline-flex flex-col items-start gap-1.5 relative flex-[0_0_auto]">
          <h1 className="w-fit mt-[-1.00px] text-[#2411a7] text-3xl leading-[37.5px] whitespace-nowrap relative [font-family:'Inter-Bold',Helvetica] font-bold tracking-[0]">
            Â¡Bienvenida!
          </h1>
          <p className="w-fit text-[#2411a7] text-lg leading-[22.5px] whitespace-nowrap relative [font-family:'Inter-Regular',Helvetica] font-normal tracking-[0]">
            Resumen de tu negocio
          </p>
        </div>
        <div
          className="inline-flex flex-col h-[59px] items-start gap-1.5 p-3 relative flex-[0_0_auto] rounded-lg overflow-hidden border border-solid border-[#ddd8ff]"
          aria-label="Fecha y hora actual"
        >
          <time
            dateTime="2025-09-26"
            className="w-fit mt-[-1.00px] text-[#2411a7] text-[13px] leading-[16.2px] whitespace-nowrap relative [font-family:'Inter-Regular',Helvetica] font-normal tracking-[0]"
          >
            Fecha: 26/09/2025
          </time>
          <time
            dateTime="2025-09-26T10:24:00"
            className="w-fit mb-[-2.00px] text-[#2411a7] text-[13px] leading-[16.2px] whitespace-nowrap relative [font-family:'Inter-Regular',Helvetica] font-normal tracking-[0]"
          >
            10:24 a. m.
          </time>
        </div>
      </header>
      <section
        aria-label="Resumen de indicadores"
        className="gap-5 w-full flex-[0_0_auto] flex items-start relative self-stretch"
      >
        {metrics.map((metric) => (
          <article
            key={metric.title}
            className={`${metric.background} flex h-[140px] items-start gap-3.5 p-[22px] relative flex-1 grow rounded-xl overflow-hidden`}
          >
            <div className="relative w-[43px] h-[43px]" aria-hidden="true">
              <img className={metric.iconClassName} alt="" src={metric.icon} />
            </div>
            <div className="flex flex-col items-start gap-3 relative flex-1 grow">
              <h2 className="w-fit mt-[-1.00px] text-white text-base leading-5 whitespace-nowrap relative [font-family:'Inter-Bold',Helvetica] font-bold tracking-[0]">
                {metric.title}
              </h2>
              <p className="w-fit text-white text-[27px] leading-[33.8px] whitespace-nowrap relative [font-family:'Inter-Bold',Helvetica] font-bold tracking-[0]">
                {metric.value}
              </p>
              {metric.description && (
                <p className="w-fit text-white text-[13px] leading-[16.2px] whitespace-nowrap relative [font-family:'Inter-Regular',Helvetica] font-normal tracking-[0]">
                  {metric.description}
                </p>
              )}
            </div>
          </article>
        ))}
      </section>
      <section
        aria-label="Resumen de ventas y productos"
        className="gap-5 w-full flex-[0_0_auto] flex items-start relative self-stretch"
      >
        <section className="w-[628px] gap-[5px] pl-5 pr-[23px] py-5 flex flex-col h-[350px] items-start relative bg-white rounded-xl border border-solid border-[#ddd8ff]">
          <h2 className="w-fit mt-[-1.00px] text-[#2411a7] text-[19px] leading-[23.8px] whitespace-nowrap relative [font-family:'Inter-Bold',Helvetica] font-bold tracking-[0]">
            Ventas recientes
          </h2>
          <div className="flex flex-col items-start relative self-stretch w-full flex-[0_0_auto] rounded-lg overflow-hidden border border-solid border-[#ddd8ff]">
            <div className="flex h-[38px] items-start gap-3 px-3 py-0 relative self-stretch w-full bg-[#f1eeff] overflow-hidden">
              <div className="w-[92px] overflow-hidden flex items-center relative self-stretch">
                <div className="w-fit text-[#2411a7] text-[13px] leading-[16.2px] whitespace-nowrap relative [font-family:'Inter-Bold',Helvetica] font-bold tracking-[0]">
                  # Venta
                </div>
                <div className="relative w-[100px] h-[100px] mt-[-31.00px] mb-[-31.00px]" />
              </div>
              <div className="w-[212px] flex items-center relative self-stretch">
                <div className="w-fit text-[#2411a7] text-[13px] leading-[16.2px] whitespace-nowrap relative [font-family:'Inter-Bold',Helvetica] font-bold tracking-[0]">
                  Fecha
                </div>
              </div>
              <div className="w-[86px] flex items-center relative self-stretch">
                <div className="w-fit text-[#2411a7] text-[13px] leading-[16.2px] whitespace-nowrap relative [font-family:'Inter-Bold',Helvetica] font-bold tracking-[0]">
                  Total
                </div>
              </div>
              <div className="w-[150px] mr-[-12.00px] flex items-center relative self-stretch">
                <div className="w-fit text-[#2411a7] text-[13px] leading-[16.2px] whitespace-nowrap relative [font-family:'Inter-Bold',Helvetica] font-bold tracking-[0]">
                  MÃ©todo de pago
                </div>
              </div>
            </div>
            {sales.map((sale) => (
              <div
                key={sale.id}
                className="flex min-h-[46px] items-center gap-3 px-3 py-2.5 relative self-stretch w-full flex-[0_0_auto] border-b [border-bottom-style:solid] border-[#ddd8ff]"
              >
                <div className="w-20 flex items-start relative">
                  <div className="flex-1 mt-[-1.00px] text-[#2411a7] text-[13px] leading-[16.2px] relative [font-family:'Inter-Regular',Helvetica] font-normal tracking-[0]">
                    {sale.id}
                  </div>
                </div>
                <div className="w-[212px] flex items-start relative">
                  <time className="flex-1 mt-[-1.00px] text-[#2411a7] text-[13px] leading-[16.2px] relative [font-family:'Inter-Regular',Helvetica] font-normal tracking-[0]">
                    {sale.date}
                  </time>
                </div>
                <div className="w-[86px] flex items-start relative">
                  <div className="flex-1 mt-[-1.00px] text-[#2411a7] text-[13px] leading-[16.2px] relative [font-family:'Inter-Regular',Helvetica] font-normal tracking-[0]">
                    {sale.total}
                  </div>
                </div>
                <div className="w-[150px] mr-[-3.00px] flex items-start relative">
                  <div className="flex-1 mt-[-1.00px] text-[#2411a7] text-[13px] leading-[16.2px] relative [font-family:'Inter-Regular',Helvetica] font-normal tracking-[0]">
                    {sale.payment}
                  </div>
                </div>
              </div>
            ))}
          </div>
          <div className="flex items-start justify-end relative self-stretch w-full flex-[0_0_auto]">
            <button
              type="button"
              className="inline-flex h-8 items-center justify-center gap-2 px-4 py-0 relative flex-[0_0_auto] bg-[#eee8ff] rounded-[10px] overflow-hidden cursor-pointer"
            >
              <span className="w-fit text-[#7051ea] text-[13px] leading-[16.2px] whitespace-nowrap relative [font-family:'Inter-Bold',Helvetica] font-bold tracking-[0]">
                Ver todas
              </span>
            </button>
          </div>
        </section>
        <section className="gap-3.5 p-5 flex-1 grow flex flex-col h-[350px] items-start relative bg-white rounded-xl border border-solid border-[#ddd8ff]">
          <h2 className="w-fit mt-[-1.00px] text-[#2411a7] text-[19px] leading-[23.8px] whitespace-nowrap relative [font-family:'Inter-Bold',Helvetica] font-bold tracking-[0]">
            Productos mÃ¡s vendidos
          </h2>
          {products.map((product, index) => (
            <div
              key={product.name}
              className={`flex h-11 items-center gap-3.5 relative self-stretch w-full ${
                index === products.length - 1 ? "mb-[-4.00px] " : ""
              }border-b [border-bottom-style:solid] border-[#ddd8ff]`}
            >
              <img
                className="relative w-[37px] h-[37px]"
                alt=""
                src={product.image}
              />
              <div className="flex-1 text-[#2411a7] text-sm leading-[17.5px] relative [font-family:'Inter-Bold',Helvetica] font-bold tracking-[0]">
                {product.name}
              </div>
              <div className="w-fit text-[#2411a7] text-sm leading-[17.5px] whitespace-nowrap relative [font-family:'Inter-Regular',Helvetica] font-normal tracking-[0]">
                {product.quantity}
              </div>
            </div>
          ))}
        </section>
      </section>
      <footer className="flex items-center gap-4 px-0 py-3 relative self-stretch w-full flex-[0_0_auto]">
        <div className="relative w-[18px] h-[18px]" aria-hidden="true">
          <img
            className="absolute w-[96.67%] h-[88.40%] top-[11.60%] left-[3.33%]"
            alt=""
            src={vector17}
          />
        </div>
        <div className="w-fit text-[#2411a7] text-xs leading-[15px] whitespace-nowrap relative [font-family:'Inter-Bold',Helvetica] font-bold tracking-[0]">
          PapelerÃ­a &amp; Regalos â™¡
        </div>
        <p className="w-fit text-[#655ac5] text-xs leading-[15px] whitespace-nowrap relative [font-family:'Inter-Regular',Helvetica] font-normal tracking-[0]">
          |&nbsp;&nbsp; Â¡Gracias por tu preferencia!
        </p>
      </footer>
    </main>
  );
};

