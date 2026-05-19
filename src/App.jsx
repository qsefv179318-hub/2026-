import React, { useMemo, useState } from "react";
import { motion } from "framer-motion";
import {
  UploadCloud,
  FileText,
  Wallet,
  ShieldCheck,
  BellRing,
  CheckCircle2,
  AlertTriangle,
  CreditCard,
  PiggyBank,
  Sparkles,
  Home,
  Upload,
  LineChart,
  UserRound,
  Settings,
  Search,
  Menu,
  ChevronRight,
  ReceiptText,
  Landmark,
} from "lucide-react";

const uploadedFiles = [
  {
    id: 1,
    name: "거래내역_2026_05.csv",
    type: "카드/계좌 거래내역",
    status: "분석 완료",
    size: "1.8MB",
  },
  {
    id: 2,
    name: "연말정산_간소화.pdf",
    type: "연말정산 간소화 자료",
    status: "분석 중",
    size: "3.2MB",
  },
];

const recentTransactions = [
  {
    id: 1,
    title: "스타벅스",
    category: "카페",
    method: "신용카드",
    amount: "6,800원",
    insight: "공제 반영 가능 소비",
  },
  {
    id: 2,
    title: "약국",
    category: "의료비",
    method: "체크카드",
    amount: "18,000원",
    insight: "의료비 항목 후보",
  },
  {
    id: 3,
    title: "대중교통",
    category: "교통",
    method: "체크카드",
    amount: "1,450원",
    insight: "대중교통 공제 후보",
  },
  {
    id: 4,
    title: "온라인 쇼핑",
    category: "생활",
    method: "신용카드",
    amount: "52,000원",
    insight: "일반 소비 항목",
  },
];

const feedbacks = [
  {
    id: 1,
    type: "good",
    title: "체크카드 공제 구간 활용 가능성이 높습니다",
    description:
      "현재 소비 흐름상 신용카드보다 체크카드 사용 비중을 일부 높이면 소득공제 효율을 개선할 여지가 있습니다.",
    tag: "소득공제",
    amount: "+84,000원 예상",
  },
  {
    id: 2,
    type: "warning",
    title: "의료비 공제 기준 도달까지 여유가 있습니다",
    description:
      "의료비 지출은 총급여 기준 일정 비율을 초과해야 공제 효과가 커집니다. 현재는 공제 반영 가능성이 낮은 구간입니다.",
    tag: "의료비",
    amount: "기준 미도달",
  },
  {
    id: 3,
    type: "tip",
    title: "연금저축 납입 한도 확인이 필요합니다",
    description:
      "예상 연봉과 기존 납입액을 기준으로 연금저축 또는 IRP 추가 납입 여부를 검토할 수 있습니다.",
    tag: "연금/IRP",
    amount: "검토 추천",
  },
];

const navItems = [
  { id: "dashboard", label: "홈", icon: Home },
  { id: "upload", label: "자료 업로드", icon: Upload },
  { id: "salary", label: "연봉 입력", icon: Wallet },
  { id: "feedback", label: "피드백", icon: BellRing },
  { id: "report", label: "리포트", icon: LineChart },
];

function cn(...classes) {
  return classes.filter(Boolean).join(" ");
}

function Sidebar({ activePage, setActivePage }) {
  return (
    <aside className="hidden h-screen w-72 shrink-0 border-r border-slate-200 bg-white px-5 py-6 lg:sticky lg:top-0 lg:block">
      <div className="flex items-center gap-3 px-2">
        <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-950 text-white shadow-lg shadow-slate-200">
          <PiggyBank size={23} />
        </div>
        <div>
          <h1 className="text-lg font-black tracking-tight text-slate-950">TaxFit</h1>
          <p className="text-xs font-semibold text-slate-400">절세 피드백 웹앱</p>
        </div>
      </div>

      <nav className="mt-9 space-y-1">
        {navItems.map((item) => {
          const Icon = item.icon;
          const active = activePage === item.id;
          return (
            <button
              key={item.id}
              onClick={() => setActivePage(item.id)}
              className={cn(
                "flex w-full items-center gap-3 rounded-2xl px-4 py-3 text-sm font-bold transition",
                active
                  ? "bg-slate-950 text-white shadow-lg shadow-slate-200"
                  : "text-slate-500 hover:bg-slate-100 hover:text-slate-950"
              )}
            >
              <Icon size={19} />
              {item.label}
            </button>
          );
        })}
      </nav>

      <div className="absolute bottom-6 left-5 right-5 rounded-3xl bg-slate-950 p-5 text-white">
        <div className="mb-4 flex h-11 w-11 items-center justify-center rounded-2xl bg-white/10">
          <ShieldCheck size={22} />
        </div>
        <p className="text-sm font-bold">올해 절세 가능성</p>
        <p className="mt-2 text-2xl font-black">246,000원</p>
        <p className="mt-2 text-xs leading-5 text-slate-300">업로드 자료와 예상 연봉 기준의 추정값입니다.</p>
      </div>
    </aside>
  );
}

function MobileBottomNav({ activePage, setActivePage }) {
  return (
    <nav className="fixed bottom-0 left-0 right-0 z-30 border-t border-slate-200 bg-white/95 px-3 py-2 backdrop-blur lg:hidden">
      <div className="grid grid-cols-5 gap-1">
        {navItems.map((item) => {
          const Icon = item.icon;
          const active = activePage === item.id;
          return (
            <button
              key={item.id}
              onClick={() => setActivePage(item.id)}
              className={cn(
                "flex flex-col items-center justify-center rounded-2xl px-2 py-2 text-[11px] font-bold transition",
                active ? "bg-slate-950 text-white" : "text-slate-400"
              )}
            >
              <Icon size={18} />
              <span className="mt-1">{item.label}</span>
            </button>
          );
        })}
      </div>
    </nav>
  );
}

function Topbar({ activePage }) {
  const titleMap = {
    dashboard: "홈 대시보드",
    upload: "자료 업로드",
    salary: "예상 연봉 입력",
    feedback: "실시간 피드백",
    report: "절세 리포트",
  };

  return (
    <header className="sticky top-0 z-20 border-b border-slate-200 bg-slate-50/90 px-4 py-4 backdrop-blur md:px-8">
      <div className="flex items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <button className="flex h-10 w-10 items-center justify-center rounded-2xl bg-white text-slate-600 shadow-sm ring-1 ring-slate-200 lg:hidden">
            <Menu size={20} />
          </button>
          <div>
            <p className="text-xs font-bold text-slate-400">TaxFit Web App</p>
            <h2 className="text-xl font-black text-slate-950 md:text-2xl">{titleMap[activePage]}</h2>
          </div>
        </div>

        <div className="hidden items-center gap-3 md:flex">
          <div className="flex h-11 w-72 items-center gap-2 rounded-2xl bg-white px-4 text-slate-400 shadow-sm ring-1 ring-slate-200">
            <Search size={18} />
            <span className="text-sm font-medium">거래, 공제항목 검색</span>
          </div>
          <button className="flex h-11 w-11 items-center justify-center rounded-2xl bg-white text-slate-600 shadow-sm ring-1 ring-slate-200">
            <Settings size={19} />
          </button>
          <button className="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-950 text-white shadow-lg shadow-slate-200">
            <UserRound size={19} />
          </button>
        </div>
      </div>
    </header>
  );
}

function StatCard({ icon: Icon, label, value, caption }) {
  return (
    <div className="rounded-[1.75rem] bg-white p-5 shadow-sm ring-1 ring-slate-100">
      <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-2xl bg-slate-100 text-slate-800">
        <Icon size={22} />
      </div>
      <p className="text-sm font-bold text-slate-400">{label}</p>
      <p className="mt-2 text-2xl font-black text-slate-950">{value}</p>
      <p className="mt-2 text-xs leading-5 text-slate-500">{caption}</p>
    </div>
  );
}

function UploadCard({ icon: Icon, title, description, acceptLabel }) {
  return (
    <div className="group rounded-[1.75rem] border border-dashed border-slate-300 bg-white p-6 shadow-sm transition hover:border-slate-950 hover:shadow-xl">
      <div className="mb-5 flex h-12 w-12 items-center justify-center rounded-2xl bg-slate-100 text-slate-800 transition group-hover:bg-slate-950 group-hover:text-white">
        <Icon size={24} />
      </div>
      <h3 className="text-lg font-black text-slate-950">{title}</h3>
      <p className="mt-2 min-h-12 text-sm leading-6 text-slate-500">{description}</p>
      <button className="mt-6 flex w-full items-center justify-center gap-2 rounded-2xl bg-slate-950 px-4 py-3 text-sm font-bold text-white transition hover:-translate-y-0.5 hover:shadow-lg">
        <UploadCloud size={18} />
        {acceptLabel}
      </button>
    </div>
  );
}

function UploadedFileList() {
  return (
    <section className="rounded-[1.75rem] bg-white p-6 shadow-sm ring-1 ring-slate-100">
      <div className="mb-5 flex items-center justify-between">
        <div>
          <p className="text-sm font-bold text-slate-400">업로드된 파일</p>
          <h3 className="text-xl font-black text-slate-950">자료 분석 상태</h3>
        </div>
        <span className="rounded-full bg-emerald-50 px-3 py-1 text-xs font-bold text-emerald-700">2개 연결됨</span>
      </div>
      <div className="space-y-3">
        {uploadedFiles.map((file) => (
          <div key={file.id} className="flex items-center justify-between rounded-2xl bg-slate-50 p-4">
            <div className="flex min-w-0 items-center gap-3">
              <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-white text-slate-700 shadow-sm">
                <FileText size={20} />
              </div>
              <div className="min-w-0">
                <p className="truncate font-bold text-slate-950">{file.name}</p>
                <p className="truncate text-xs text-slate-500">{file.type} · {file.size}</p>
              </div>
            </div>
            <span
              className={cn(
                "ml-3 shrink-0 rounded-full px-3 py-1 text-xs font-bold",
                file.status === "분석 완료" ? "bg-emerald-100 text-emerald-700" : "bg-amber-100 text-amber-700"
              )}
            >
              {file.status}
            </span>
          </div>
        ))}
      </div>
    </section>
  );
}

function DashboardPage({ setActivePage }) {
  return (
    <motion.div initial={{ opacity: 0, y: 14 }} animate={{ opacity: 1, y: 0 }} className="space-y-6">
      <section className="rounded-[2rem] bg-slate-950 p-6 text-white shadow-2xl shadow-slate-200 md:p-8">
        <div className="grid gap-8 lg:grid-cols-[1.1fr_0.9fr] lg:items-center">
          <div>
            <div className="mb-5 inline-flex items-center gap-2 rounded-full bg-white/10 px-4 py-2 text-sm font-bold text-slate-200">
              <Sparkles size={17} />
              실시간 절세 어시스턴트
            </div>
            <h1 className="text-3xl font-black tracking-tight md:text-5xl">
              거래할 때마다,
              <br />
              절세 가능성을 바로 확인하세요
            </h1>
            <p className="mt-5 max-w-2xl text-sm leading-7 text-slate-300 md:text-base">
              거래내역, 연말정산 간소화 자료, 예상 연봉을 연결해 소비 패턴을 분석하고 공제 가능성이 있는 항목을 웹앱 안에서 바로 안내합니다.
            </p>
            <div className="mt-7 flex flex-col gap-3 sm:flex-row">
              <button
                onClick={() => setActivePage("upload")}
                className="rounded-2xl bg-white px-5 py-3 text-sm font-black text-slate-950 transition hover:-translate-y-0.5"
              >
                자료 업로드 시작
              </button>
              <button
                onClick={() => setActivePage("feedback")}
                className="rounded-2xl bg-white/10 px-5 py-3 text-sm font-black text-white transition hover:bg-white/15"
              >
                피드백 보기
              </button>
            </div>
          </div>
          <div className="rounded-[1.75rem] bg-white p-5 text-slate-950">
            <p className="text-sm font-bold text-slate-400">이번 달 분석 요약</p>
            <div className="mt-5 space-y-4">
              {[
                ["공제 후보 거래", "18건"],
                ["체크카드 추천 비중", "높음"],
                ["연금/IRP 추가 검토", "필요"],
              ].map(([label, value]) => (
                <div key={label} className="flex items-center justify-between rounded-2xl bg-slate-50 p-4">
                  <span className="text-sm font-bold text-slate-500">{label}</span>
                  <span className="text-lg font-black text-slate-950">{value}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-3">
        <StatCard icon={ReceiptText} label="이번 달 소비" value="1,284,500원" caption="업로드된 거래내역 기준 추정값" />
        <StatCard icon={ShieldCheck} label="예상 절세 효과" value="246,000원" caption="입력 자료 기반의 참고용 예상값" />
        <StatCard icon={Landmark} label="공제 후보 항목" value="5개" caption="카드, 의료비, 교통, 연금 항목 포함" />
      </section>
    </motion.div>
  );
}

function UploadPage() {
  return (
    <motion.div initial={{ opacity: 0, y: 14 }} animate={{ opacity: 1, y: 0 }} className="space-y-6">
      <section className="grid gap-5 xl:grid-cols-[1fr_1fr_1.2fr]">
        <UploadCard
          icon={CreditCard}
          title="거래내역 파일"
          description="카드사, 은행, 가계부 앱에서 내려받은 CSV, XLSX 파일을 업로드합니다."
          acceptLabel="거래내역 업로드"
        />
        <UploadCard
          icon={FileText}
          title="연말정산 간소화 파일"
          description="국세청 연말정산 간소화 PDF 또는 정리된 자료를 업로드합니다."
          acceptLabel="간소화 자료 업로드"
        />
        <UploadedFileList />
      </section>

      <section className="rounded-[1.75rem] bg-white p-6 shadow-sm ring-1 ring-slate-100">
        <h3 className="text-xl font-black text-slate-950">파일 업로드 후 처리 흐름</h3>
        <div className="mt-5 grid gap-4 md:grid-cols-3">
          {[
            ["1", "거래 데이터 정리", "날짜, 금액, 결제수단, 거래처명을 표준화합니다."],
            ["2", "공제 항목 매핑", "의료비, 교통비, 카드 사용액 등 후보 항목을 분류합니다."],
            ["3", "피드백 생성", "예상 연봉과 함께 절세 가능성을 계산합니다."],
          ].map(([step, title, desc]) => (
            <div key={step} className="rounded-2xl bg-slate-50 p-5">
              <div className="mb-4 flex h-9 w-9 items-center justify-center rounded-xl bg-slate-950 text-sm font-black text-white">{step}</div>
              <p className="font-black text-slate-950">{title}</p>
              <p className="mt-2 text-sm leading-6 text-slate-500">{desc}</p>
            </div>
          ))}
        </div>
      </section>
    </motion.div>
  );
}

function SalaryPage({ salary, setSalary }) {
  const formattedSalary = useMemo(() => {
    const numberValue = Number(String(salary).replaceAll(",", ""));
    if (!numberValue) return "0";
    return numberValue.toLocaleString("ko-KR");
  }, [salary]);

  return (
    <motion.div initial={{ opacity: 0, y: 14 }} animate={{ opacity: 1, y: 0 }} className="grid gap-6 xl:grid-cols-[1.1fr_0.9fr]">
      <section className="rounded-[2rem] bg-slate-950 p-6 text-white shadow-2xl shadow-slate-200 md:p-8">
        <div className="mb-8 flex items-center justify-between">
          <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-white/10">
            <Wallet size={28} />
          </div>
          <span className="rounded-full bg-white/10 px-4 py-2 text-sm font-bold text-slate-200">예상 연봉 입력</span>
        </div>
        <h2 className="text-3xl font-black tracking-tight">올해 예상 총급여를 입력해주세요</h2>
        <p className="mt-4 max-w-xl text-sm leading-7 text-slate-300">
          연봉 정보는 카드 사용 공제 기준, 의료비 공제 가능성, 연금저축·IRP 추천 한도 계산의 기준값으로 사용됩니다.
        </p>
        <div className="mt-8 rounded-[1.75rem] bg-white p-5 text-slate-950">
          <label className="text-sm font-bold text-slate-500">예상 연봉</label>
          <div className="mt-3 flex items-end gap-3">
            <input
              value={salary}
              onChange={(event) => setSalary(event.target.value.replace(/[^0-9]/g, ""))}
              placeholder="예: 42000000"
              className="w-full border-none bg-transparent text-3xl font-black outline-none placeholder:text-slate-300 md:text-5xl"
            />
            <span className="pb-2 text-lg font-bold text-slate-500">원</span>
          </div>
          <p className="mt-4 rounded-2xl bg-slate-100 px-4 py-3 text-sm font-bold text-slate-600">
            입력값 미리보기: {formattedSalary}원
          </p>
        </div>
      </section>

      <section className="rounded-[2rem] bg-white p-6 shadow-sm ring-1 ring-slate-100">
        <h3 className="text-xl font-black text-slate-950">분석에 활용되는 기준</h3>
        <div className="mt-5 space-y-4">
          {[
            ["카드 사용 공제 기준", "총급여 대비 사용액 구간 확인"],
            ["의료비 공제 가능성", "소득 대비 의료비 지출 비율 추정"],
            ["연금저축·IRP 전략", "납입 한도와 예상 절세 효과 계산"],
            ["실시간 소비 피드백", "거래 발생 시 절세 관점 알림 제공"],
          ].map(([title, desc]) => (
            <div key={title} className="flex items-start gap-3 rounded-2xl bg-slate-50 p-4">
              <CheckCircle2 className="mt-0.5 text-emerald-600" size={20} />
              <div>
                <p className="font-black text-slate-900">{title}</p>
                <p className="mt-1 text-sm text-slate-500">{desc}</p>
              </div>
            </div>
          ))}
        </div>
      </section>
    </motion.div>
  );
}

function FeedbackCard({ item }) {
  const styles = {
    good: "bg-emerald-50 text-emerald-700 ring-emerald-100",
    warning: "bg-amber-50 text-amber-700 ring-amber-100",
    tip: "bg-indigo-50 text-indigo-700 ring-indigo-100",
  };

  const Icon = item.type === "warning" ? AlertTriangle : item.type === "tip" ? Sparkles : ShieldCheck;

  return (
    <div className="rounded-[1.75rem] bg-white p-5 shadow-sm ring-1 ring-slate-100">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div className="flex gap-4">
          <div className={cn("flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl ring-1", styles[item.type])}>
            <Icon size={22} />
          </div>
          <div>
            <div className="mb-2 flex flex-wrap items-center gap-2">
              <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-bold text-slate-600">{item.tag}</span>
              <span className="text-xs font-bold text-slate-400">실시간 피드백</span>
            </div>
            <h4 className="font-black text-slate-950">{item.title}</h4>
            <p className="mt-2 text-sm leading-6 text-slate-500">{item.description}</p>
          </div>
        </div>
        <p className="w-fit shrink-0 rounded-full bg-slate-950 px-3 py-1 text-xs font-bold text-white">{item.amount}</p>
      </div>
    </div>
  );
}

function FeedbackPage() {
  return (
    <motion.div initial={{ opacity: 0, y: 14 }} animate={{ opacity: 1, y: 0 }} className="grid gap-6 xl:grid-cols-[0.95fr_1.05fr]">
      <section className="rounded-[2rem] bg-white p-6 shadow-sm ring-1 ring-slate-100">
        <div className="mb-6 flex items-center justify-between">
          <div>
            <p className="text-sm font-bold text-slate-400">최근 거래</p>
            <h3 className="text-xl font-black text-slate-950">거래별 절세 인사이트</h3>
          </div>
          <BellRing className="text-slate-500" size={22} />
        </div>
        <div className="space-y-3">
          {recentTransactions.map((transaction) => (
            <div key={transaction.id} className="flex items-center justify-between gap-4 rounded-2xl bg-slate-50 p-4">
              <div className="flex min-w-0 items-center gap-3">
                <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-white text-slate-700 shadow-sm">
                  <CreditCard size={19} />
                </div>
                <div className="min-w-0">
                  <p className="truncate font-black text-slate-950">{transaction.title}</p>
                  <p className="truncate text-xs text-slate-500">{transaction.category} · {transaction.method}</p>
                </div>
              </div>
              <div className="shrink-0 text-right">
                <p className="font-black text-slate-950">{transaction.amount}</p>
                <p className="mt-1 text-xs font-bold text-emerald-600">{transaction.insight}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      <section className="space-y-4">
        {feedbacks.map((item) => (
          <FeedbackCard key={item.id} item={item} />
        ))}
      </section>
    </motion.div>
  );
}

function ReportPage() {
  return (
    <motion.div initial={{ opacity: 0, y: 14 }} animate={{ opacity: 1, y: 0 }} className="space-y-6">
      <section className="grid gap-4 md:grid-cols-3">
        <StatCard icon={ShieldCheck} label="예상 절세" value="246,000원" caption="현재 입력 자료 기준의 참고용 추정값" />
        <StatCard icon={CreditCard} label="카드 공제 후보" value="18건" caption="거래내역에서 분류된 후보 항목" />
        <StatCard icon={Wallet} label="추가 검토 항목" value="3개" caption="연금, 의료비, 교통비 항목" />
      </section>

      <section className="rounded-[2rem] bg-white p-6 shadow-sm ring-1 ring-slate-100">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm font-bold text-slate-400">연말정산 리포트</p>
            <h3 className="text-xl font-black text-slate-950">공제 항목별 진행률</h3>
          </div>
          <button className="rounded-2xl bg-slate-950 px-4 py-3 text-sm font-black text-white">리포트 내보내기</button>
        </div>
        <div className="mt-6 space-y-5">
          {[
            ["카드 사용액", "72%"],
            ["의료비", "38%"],
            ["연금저축/IRP", "54%"],
            ["대중교통", "66%"],
          ].map(([label, percent]) => (
            <div key={label}>
              <div className="mb-2 flex items-center justify-between text-sm font-bold">
                <span className="text-slate-600">{label}</span>
                <span className="text-slate-950">{percent}</span>
              </div>
              <div className="h-3 rounded-full bg-slate-100">
                <div className="h-3 rounded-full bg-slate-950" style={{ width: percent }} />
              </div>
            </div>
          ))}
        </div>
      </section>
    </motion.div>
  );
}

export default function TaxSavingWebApp() {
  const [activePage, setActivePage] = useState("dashboard");
  const [salary, setSalary] = useState("42000000");

  const renderPage = () => {
    if (activePage === "dashboard") return <DashboardPage setActivePage={setActivePage} />;
    if (activePage === "upload") return <UploadPage />;
    if (activePage === "salary") return <SalaryPage salary={salary} setSalary={setSalary} />;
    if (activePage === "feedback") return <FeedbackPage />;
    if (activePage === "report") return <ReportPage />;
    return <DashboardPage setActivePage={setActivePage} />;
  };

  return (
    <div className="min-h-screen bg-slate-50 text-slate-950">
      <div className="flex min-h-screen">
        <Sidebar activePage={activePage} setActivePage={setActivePage} />
        <div className="min-w-0 flex-1 pb-24 lg:pb-0">
          <Topbar activePage={activePage} />
          <main className="mx-auto max-w-7xl px-4 py-6 md:px-8 md:py-8">{renderPage()}</main>
        </div>
      </div>
      <MobileBottomNav activePage={activePage} setActivePage={setActivePage} />
    </div>
  );
}
