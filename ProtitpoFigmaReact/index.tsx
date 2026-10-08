import { useMemo, useState } from "react";

const navigationItems = [
  { label: "Overview", icon: "grid", active: true },
  { label: "Analytics", icon: "chart" },
  { label: "Customers", icon: "users" },
  { label: "Transactions", icon: "wallet" },
];

const secondaryNavigationItems = [
  { label: "Settings", icon: "settings" },
  { label: "Help center", icon: "help" },
];

const metrics = [
  {
    label: "Total revenue",
    value: "$84,254.80",
    change: "+12.5%",
    detail: "vs. previous month",
    icon: "dollar",
    color: "bg-violet-100 text-violet-600",
  },
  {
    label: "Active customers",
    value: "12,480",
    change: "+8.2%",
    detail: "vs. previous month",
    icon: "users",
    color: "bg-blue-100 text-blue-600",
  },
  {
    label: "Total orders",
    value: "8,642",
    change: "+4.6%",
    detail: "vs. previous month",
    icon: "shopping",
    color: "bg-emerald-100 text-emerald-600",
  },
  {
    label: "Conversion rate",
    value: "6.84%",
    change: "+2.4%",
    detail: "vs. previous month",
    icon: "target",
    color: "bg-orange-100 text-orange-600",
  },
];

const recentTransactions = [
  {
    customer: "Olivia Martin",
    email: "olivia.martin@example.com",
    amount: "$1,240.00",
    status: "Completed",
    initials: "OM",
    avatar: "bg-pink-100 text-pink-600",
  },
  {
    customer: "Jackson Lee",
    email: "jackson.lee@example.com",
    amount: "$890.50",
    status: "Completed",
    initials: "JL",
    avatar: "bg-blue-100 text-blue-600",
  },
  {
    customer: "Sophia Brown",
    email: "sophia.brown@example.com",
    amount: "$675.00",
    status: "Pending",
    initials: "SB",
    avatar: "bg-amber-100 text-amber-600",
  },
  {
    customer: "Noah Wilson",
    email: "noah.wilson@example.com",
    amount: "$2,140.75",
    status: "Completed",
    initials: "NW",
    avatar: "bg-emerald-100 text-emerald-600",
  },
  {
    customer: "Emma Davis",
    email: "emma.davis@example.com",
    amount: "$430.00",
    status: "Failed",
    initials: "ED",
    avatar: "bg-violet-100 text-violet-600",
  },
];

function Icon({ name, size = 20, strokeWidth = 1.8 }) {
  const common = {
    width: size,
    height: size,
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: "currentColor",
    strokeWidth,
    strokeLinecap: "round",
    strokeLinejoin: "round",
    "aria-hidden": true,
  };

  const paths = {
    grid: (
      <>
        <rect x="3" y="3" width="7" height="7" rx="1" />
        <rect x="14" y="3" width="7" height="7" rx="1" />
        <rect x="3" y="14" width="7" height="7" rx="1" />
        <rect x="14" y="14" width="7" height="7" rx="1" />
      </>
    ),
    chart: (
      <>
        <path d="M4 19V5" />
        <path d="M4 19h17" />
        <path d="m7 15 4-4 3 2 6-7" />
      </>
    ),
    users: (
      <>
        <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
        <circle cx="9" cy="7" r="4" />
        <path d="M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" />
      </>
    ),
    wallet: (
      <>
        <path d="M20 7V5a2 2 0 0 0-2-2H5a3 3 0 0 0 0 6h16v10a2 2 0 0 1-2 2H5a3 3 0 0 1-3-3V6" />
        <path d="M16 14h.01" />
      </>
    ),
    settings: (
      <>
        <circle cx="12" cy="12" r="3" />
        <path d="M19.4 15a1.7 1.7 0 0 0 .34 1.88l.06.06-1.42 1.42-.06-.06a1.7 1.7 0 0 0-1.88-.34 1.7 1.7 0 0 0-1.03 1.56V21h-2v-.09a1.7 1.7 0 0 0-1.03-1.56 1.7 1.7 0 0 0-1.88.34l-.06.06-1.42-1.42.06-.06A1.7 1.7 0 0 0 9.4 15a1.7 1.7 0 0 0-1.56-1.03H7v-2h.84A1.7 1.7 0 0 0 9.4 11a1.7 1.7 0 0 0-.34-1.88L9 9.06l1.42-1.42.06.06a1.7 1.7 0 0 0 1.88.34A1.7 1.7 0 0 0 13.39 6.5V6h2v.5a1.7 1.7 0 0 0 1.03 1.54 1.7 1.7 0 0 0 1.88-.34l.06-.06 1.42 1.42-.06.06a1.7 1.7 0 0 0-.34 1.88 1.7 1.7 0 0 0 1.56 1.03H21v2h-.04A1.7 1.7 0 0 0 19.4 15Z" />
      </>
    ),
    help: (
      <>
        <circle cx="12" cy="12" r="9" />
        <path d="M9.5 9a2.5 2.5 0 1 1 4.31 1.74c-.92.9-1.81 1.28-1.81 2.76" />
        <path d="M12 17h.01" />
      </>
    ),
    bell: (
      <>
        <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4" />
      </>
    ),
    search: (
      <>
        <circle cx="11" cy="11" r="7" />
        <path d="m20 20-4-4" />
      </>
    ),
    chevron: <path d="m6 9 6 6 6-6" />,
    dollar: (
      <>
        <path d="M12 2v20M17 5.5C16.1 4.6 14.7 4 13 4h-2a4 4 0 0 0 0 8h2a4 4 0 0 1 0 8h-2c-1.7 0-3.1-.6-4-1.5" />
      </>
    ),
    shopping: (
      <>
        <path d="M3 3h2l2.4 11.2a2 2 0 0 0 2 1.6h7.8a2 2 0 0 0 1.9-1.5L21 7H6" />
        <circle cx="10" cy="20" r="1" />
        <circle cx="18" cy="20" r="1" />
      </>
    ),
    target: (
      <>
        <circle cx="12" cy="12" r="8" />
        <circle cx="12" cy="12" r="4" />
        <circle cx="12" cy="12" r="1" />
      </>
    ),
    arrow: <path d="m5 12 4 4 10-10" />,
    more: (
      <>
        <circle cx="5" cy="12" r="1" fill="currentColor" />
        <circle cx="12" cy="12" r="1" fill="currentColor" />
        <circle cx="19" cy="12" r="1" fill="currentColor" />
      </>
    ),
  };

  return <svg {...common}>{paths[name]}</svg>;
}

export function DashboardHeaderSection({ onMenuClick }) {
  return (
    <header className="sticky top-0 z-20 flex h-[72px] w-full items-center justify-between border-b border-[#e8e5f3] bg-white px-5 sm:px-8">
      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={onMenuClick}
          className="mr-1 rounded-lg p-2 text-[#6d6a7d] hover:bg-[#f5f3fb] lg:hidden"
          aria-label="Open navigation"
        >
          <Icon name="grid" size={21} />
        </button>
        <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-[#635bff] text-white shadow-sm">
          <span className="text-lg font-bold">A</span>
        </div>
        <span className="text-[19px] font-bold tracking-[-0.02em] text-[#201d2b]">
          Acme<span className="text-[#635bff]">.</span>
        </span>
      </div>
      <div className="flex items-center gap-3 sm:gap-5">
        <label className="relative hidden md:block">
          <span className="sr-only">Search</span>
          <Icon
            name="search"
            size={18}
            className="absolute left-3 top-1/2 -translate-y-1/2 text-[#aaa6b8]"
          />
          <input
            type="search"
            placeholder="Search anything..."
            className="h-10 w-[220px] rounded-lg border border-[#ebe8f2] bg-[#fafafd] pl-10 pr-3 text-sm text-[#302d3d] placeholder:text-[#aaa6b8] focus:border-[#958eff]"
          />
        </label>
        <button
          type="button"
          className="relative rounded-lg p-2 text-[#777386] hover:bg-[#f5f3fb]"
          aria-label="Notifications"
        >
          <Icon name="bell" size={21} />
          <span className="absolute right-1.5 top-1.5 h-2 w-2 rounded-full border-2 border-white bg-[#635bff]" />
        </button>
        <div className="hidden h-7 w-px bg-[#ebe8f2] sm:block" />
        <button
          type="button"
          className="flex items-center gap-2.5 rounded-lg p-1 hover:bg-[#fafafd]"
        >
          <span className="flex h-9 w-9 items-center justify-center rounded-full bg-[#dcd8ff] text-sm font-semibold text-[#5148db]">
            JD
          </span>
          <span className="hidden text-left sm:block">
            <span className="block text-sm font-semibold text-[#2d2a38]">
              John Doe
            </span>
            <span className="block text-xs text-[#9692a2]">Administrator</span>
          </span>
          <Icon name="chevron" size={15} />
        </button>
      </div>
    </header>
  );
}

export function MainNavigationSection({ open, onClose }) {
  return (
    <>
      {open && (
        <button
          type="button"
          aria-label="Close navigation"
          onClick={onClose}
          className="fixed inset-0 z-30 bg-[#201d2b]/30 lg:hidden"
        />
      )}
      <aside
        className={`fixed bottom-0 left-0 top-[72px] z-40 flex w-[244px] flex-col border-r border-[#e8e5f3] bg-white px-4 py-6 transition-transform lg:static lg:z-0 lg:translate-x-0 ${
          open ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        <nav aria-label="Main navigation">
          <p className="mb-3 px-3 text-[10px] font-bold uppercase tracking-[0.14em] text-[#aaa6b8]">
            Main menu
          </p>
          <ul className="space-y-1">
            {navigationItems.map((item) => (
              <li key={item.label}>
                <button
                  type="button"
                  className={`flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition ${
                    item.active
                      ? "bg-[#f0efff] text-[#635bff]"
                      : "text-[#777386] hover:bg-[#fafafd] hover:text-[#302d3d]"
                  }`}
                >
                  <Icon name={item.icon} size={19} />
                  {item.label}
                </button>
              </li>
            ))}
          </ul>
          <p className="mb-3 mt-9 px-3 text-[10px] font-bold uppercase tracking-[0.14em] text-[#aaa6b8]">
            Support
          </p>
          <ul className="space-y-1">
            {secondaryNavigationItems.map((item) => (
              <li key={item.label}>
                <button
                  type="button"
                  className="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-[#777386] hover:bg-[#fafafd] hover:text-[#302d3d]"
                >
                  <Icon name={item.icon} size={19} />
                  {item.label}
                </button>
              </li>
            ))}
          </ul>
        </nav>
        <div className="mt-auto rounded-xl bg-[#f7f6ff] p-4">
          <div className="mb-3 flex h-8 w-8 items-center justify-center rounded-lg bg-[#e5e2ff] text-[#635bff]">
            <Icon name="target" size={17} />
          </div>
          <p className="text-sm font-semibold text-[#302d3d]">
            Upgrade your plan
          </p>
          <p className="mt-1 text-xs leading-5 text-[#888496]">
            Unlock more features and grow your business.
          </p>
          <button
            type="button"
            className="mt-3 text-xs font-bold text-[#635bff] hover:underline"
          >
            View plans â†’
          </button>
        </div>
      </aside>
    </>
  );
}

export function DashboardOverviewSection() {
  const [range, setRange] = useState("Last 30 days");
  const [showAll, setShowAll] = useState(false);
  const transactions = useMemo(
    () => (showAll ? recentTransactions : recentTransactions.slice(0, 4)),
    [showAll],
  );

  return (
    <main className="min-w-0 flex-1 bg-[#f8f7fc] px-5 py-7 sm:px-8 lg:px-10">
      <div className="mx-auto max-w-[1380px]">
        <div className="mb-7 flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
          <div>
            <p className="mb-1 text-sm text-[#8d899a]">
              Monday, October 24, 2022
            </p>
            <h1 className="text-[28px] font-bold tracking-[-0.03em] text-[#201d2b] sm:text-[32px]">
              Good morning, John <span aria-hidden="true">ðŸ‘‹</span>
            </h1>
            <p className="mt-1 text-sm text-[#888496]">
              Hereâ€™s whatâ€™s happening with your business today.
            </p>
          </div>
          <button
            type="button"
            className="flex h-10 items-center justify-center gap-2 rounded-lg bg-[#635bff] px-4 text-sm font-semibold text-white shadow-[0_4px_10px_rgba(99,91,255,0.18)] hover:bg-[#544be8]"
          >
            <span className="text-lg leading-none">+</span> Add new
          </button>
        </div>
        <section aria-labelledby="overview-heading">
          <div className="mb-4 flex items-center justify-between">
            <h2
              id="overview-heading"
              className="text-base font-bold text-[#302d3d]"
            >
              Overview
            </h2>
            <label className="relative">
              <span className="sr-only">Select date range</span>
              <select
                value={range}
                onChange={(event) => setRange(event.target.value)}
                className="h-9 rounded-lg border border-[#e5e2ee] bg-white px-3 pr-8 text-xs font-medium text-[#686477]"
              >
                <option>Last 30 days</option>
                <option>Last 7 days</option>
                <option>Last 12 months</option>
              </select>
            </label>
          </div>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
            {metrics.map((metric) => (
              <article
                key={metric.label}
                className="rounded-xl border border-[#ebe8f2] bg-white p-5 shadow-[0_1px_2px_rgba(31,28,46,0.02)]"
              >
                <div className="flex items-start justify-between">
                  <div
                    className={`flex h-10 w-10 items-center justify-center rounded-lg ${metric.color}`}
                  >
                    <Icon name={metric.icon} size={20} />
                  </div>
                  <button
                    type="button"
                    aria-label={`More options for ${metric.label}`}
                    className="text-[#aaa6b8] hover:text-[#635bff]"
                  >
                    <Icon name="more" size={19} />
                  </button>
                </div>
                <p className="mt-5 text-sm text-[#898596]">{metric.label}</p>
                <p className="mt-1 text-[23px] font-bold tracking-[-0.025em] text-[#292634]">
                  {metric.value}
                </p>
                <p className="mt-2 flex items-center gap-1 text-xs">
                  <span className="font-semibold text-[#22a06b]">
                    {metric.change}
                  </span>
                  <span className="text-[#aaa6b8]">{metric.detail}</span>
                </p>
              </article>
            ))}
          </div>
        </section>
        <div className="mt-6 grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,1.4fr)_minmax(340px,0.8fr)]">
          <section
            className="rounded-xl border border-[#ebe8f2] bg-white p-5 sm:p-6"
            aria-labelledby="revenue-heading"
          >
            <div className="flex items-start justify-between">
              <div>
                <h2
                  id="revenue-heading"
                  className="text-base font-bold text-[#302d3d]"
                >
                  Revenue overview
                </h2>
                <p className="mt-1 text-xs text-[#9692a2]">
                  Monthly revenue performance
                </p>
              </div>
              <button
                type="button"
                className="rounded-lg p-1 text-[#aaa6b8] hover:bg-[#f7f6ff]"
              >
                <Icon name="more" size={19} />
              </button>
            </div>
            <div className="mt-6 flex items-end gap-2">
              <span className="text-[28px] font-bold tracking-[-0.03em] text-[#292634]">
                $84,254
              </span>
              <span className="mb-1 text-xs font-semibold text-[#22a06b]">
                +12.5%
              </span>
            </div>
            <div className="mt-5 h-[190px] w-full">
              <svg
                viewBox="0 0 700 190"
                className="h-full w-full overflow-visible"
                role="img"
                aria-label="Revenue increased over the last six months"
              >
                {[30, 75, 120, 165].map((y) => (
                  <line
                    key={y}
                    x1="0"
                    x2="700"
                    y1={y}
                    y2={y}
                    stroke="#efedf5"
                    strokeWidth="1"
                  />
                ))}
                <path
                  d="M0 148 C38 142 45 125 82 130 S128 105 166 119 S210 88 250 100 S293 73 335 91 S380 61 420 76 S466 44 507 64 S557 38 594 51 S649 17 700 27 V190 H0Z"
                  fill="url(#area)"
                />
                <path
                  d="M0 148 C38 142 45 125 82 130 S128 105 166 119 S210 88 250 100 S293 73 335 91 S380 61 420 76 S466 44 507 64 S557 38 594 51 S649 17 700 27"
                  fill="none"
                  stroke="#635bff"
                  strokeWidth="3"
                />
                <defs>
                  <linearGradient id="area" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0" stopColor="#635bff" stopOpacity=".22" />
                    <stop offset="1" stopColor="#635bff" stopOpacity="0" />
                  </linearGradient>
                </defs>
              </svg>
            </div>
            <div className="mt-2 flex justify-between text-[11px] text-[#aaa6b8]">
              {["May", "Jun", "Jul", "Aug", "Sep", "Oct"].map((month) => (
                <span key={month}>{month}</span>
              ))}
            </div>
          </section>
          <section
            className="rounded-xl border border-[#ebe8f2] bg-white p-5 sm:p-6"
            aria-labelledby="activity-heading"
          >
            <div className="flex items-start justify-between">
              <div>
                <h2
                  id="activity-heading"
                  className="text-base font-bold text-[#302d3d]"
                >
                  Customer activity
                </h2>
                <p className="mt-1 text-xs text-[#9692a2]">
                  Active users this week
                </p>
              </div>
              <button
                type="button"
                className="rounded-lg p-1 text-[#aaa6b8] hover:bg-[#f7f6ff]"
              >
                <Icon name="more" size={19} />
              </button>
            </div>
            <div className="mt-7 flex items-center justify-center gap-8">
              <div
                className="relative flex h-[150px] w-[150px] items-center justify-center rounded-full"
                style={{
                  background:
                    "conic-gradient(#635bff 0 68%, #ddd9ff 68% 82%, #f0eff8 82% 100%)",
                }}
              >
                <div className="flex h-[108px] w-[108px] flex-col items-center justify-center rounded-full bg-white">
                  <span className="text-[25px] font-bold text-[#292634]">
                    68%
                  </span>
                  <span className="text-[11px] text-[#9692a2]">Engagement</span>
                </div>
              </div>
              <div className="space-y-4 text-xs">
                <p>
                  <span className="mr-2 inline-block h-2 w-2 rounded-full bg-[#635bff]" />
                  New users <strong className="ml-3 text-[#302d3d]">68%</strong>
                </p>
                <p>
                  <span className="mr-2 inline-block h-2 w-2 rounded-full bg-[#aaa2ff]" />
                  Returning <strong className="ml-3 text-[#302d3d]">14%</strong>
                </p>
                <p>
                  <span className="mr-2 inline-block h-2 w-2 rounded-full bg-[#e7e5f1]" />
                  Inactive <strong className="ml-3 text-[#302d3d]">18%</strong>
                </p>
              </div>
            </div>
          </section>
        </div>
        <section
          className="mt-6 rounded-xl border border-[#ebe8f2] bg-white"
          aria-labelledby="transactions-heading"
        >
          <div className="flex items-center justify-between border-b border-[#efedf5] px-5 py-5 sm:px-6">
            <div>
              <h2
                id="transactions-heading"
                className="text-base font-bold text-[#302d3d]"
              >
                Recent transactions
              </h2>
              <p className="mt-1 text-xs text-[#9692a2]">
                Your latest customer transactions
              </p>
            </div>
            <button
              type="button"
              onClick={() => setShowAll((current) => !current)}
              className="text-xs font-semibold text-[#635bff] hover:underline"
            >
              {showAll ? "Show less" : "View all"}
            </button>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full min-w-[650px] text-left">
              <thead className="bg-[#fbfaff] text-[11px] font-semibold uppercase tracking-wide text-[#aaa6b8]">
                <tr>
                  <th className="px-5 py-3 font-semibold sm:px-6">Customer</th>
                  <th className="px-5 py-3 font-semibold">Amount</th>
                  <th className="px-5 py-3 font-semibold">Status</th>
                  <th className="px-5 py-3 font-semibold">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#f1eff6]">
                {transactions.map((transaction) => (
                  <tr key={transaction.email} className="text-sm">
                    <td className="px-5 py-3.5 sm:px-6">
                      <div className="flex items-center gap-3">
                        <span
                          className={`flex h-9 w-9 items-center justify-center rounded-full text-xs font-bold ${transaction.avatar}`}
                        >
                          {transaction.initials}
                        </span>
                        <span>
                          <strong className="block text-sm font-semibold text-[#373342]">
                            {transaction.customer}
                          </strong>
                          <span className="block text-xs text-[#aaa6b8]">
                            {transaction.email}
                          </span>
                        </span>
                      </div>
                    </td>
                    <td className="px-5 py-3.5 font-semibold text-[#373342]">
                      {transaction.amount}
                    </td>
                    <td className="px-5 py-3.5">
                      <span
                        className={`rounded-full px-2.5 py-1 text-[11px] font-semibold ${transaction.status === "Completed" ? "bg-emerald-50 text-emerald-600" : transaction.status === "Pending" ? "bg-amber-50 text-amber-600" : "bg-red-50 text-red-500"}`}
                      >
                        {transaction.status}
                      </span>
                    </td>
                    <td className="px-5 py-3.5">
                      <button
                        type="button"
                        aria-label={`View ${transaction.customer}'s transaction`}
                        className="text-[#aaa6b8] hover:text-[#635bff]"
                      >
                        <Icon name="more" size={19} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      </div>
    </main>
  );
}

export const Inicio = () => {
  const [navigationOpen, setNavigationOpen] = useState(false);

  return (
    <div className="flex min-h-screen flex-col bg-[#f8f7fc] text-[#302d3d]">
      <DashboardHeaderSection onMenuClick={() => setNavigationOpen(true)} />
      <div className="flex min-h-0 flex-1">
        <MainNavigationSection
          open={navigationOpen}
          onClose={() => setNavigationOpen(false)}
        />
        <DashboardOverviewSection />
      </div>
    </div>
  );
};

export default Inicio;

