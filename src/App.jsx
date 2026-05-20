import React, { useEffect, useRef, useState } from "react";
import { motion } from "framer-motion";
import {
  UploadCloud,
  FileText,
  Wallet,
  ShieldCheck,
  BellRing,
  AlertTriangle,
  CreditCard,
  PiggyBank,
  Sparkles,
  Home,
  LineChart,
  UserRound,
  Settings,
  Search,
  ReceiptText,
  Landmark,
  CheckCircle2,
  RotateCcw,
  Menu,
  ChevronRight,
} from "lucide-react";

const API_BASE = "http://localhost:8080";
const FIXED_USER_ID = "11111111-1111-1111-1111-111111111111";

const fallbackFeedbacks = [
  { id: 1, type: "good", title: "체크카드 공제 구간 활용 가능성이 높습니다", description: "현재 소비 흐름상 신용카드보다 체크카드 사용 비중을 일부 높이면 소득공제 효율을 개선할 여지가 있습니다." },
  { id: 2, type: "warning", title: "의료비 공제 기준 도달까지 여유가 있습니다", description: "의료비 지출은 총급여 기준 일정 비율을 초과해야 공제 효과가 커집니다. 현재는 공제 반영 가능성이 낮은 구간입니다." }
];

const fallbackTransactions = [
  { id: 1, merchantName: "스타벅스", category: "카페", approvedAt: "2026-05-20", amount: 6800 },
  { id: 2, merchantName: "약국", category: "의료비", approvedAt: "2026-05-20", amount: 18000 }
];

const defaultUploadedFiles = [
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
    status: "분석 완료",
    size: "3.2MB",
  },
];

const navItems = [
  {
    id: "home",
    label: "홈",
    fullLabel: "홈 대시보드",
    icon: Home,
  },
  {
    id: "upload",
    label: "업로드",
    fullLabel: "자료 업로드",
    icon: UploadCloud,
  },
  {
    id: "salary",
    label: "연봉",
    fullLabel: "연봉 입력",
    icon: Wallet,
  },
  {
    id: "feedback",
    label: "피드백",
    fullLabel: "실시간 피드백",
    icon: BellRing,
  },
  {
    id: "report",
    label: "리포트",
    fullLabel: "절세 리포트",
    icon: LineChart,
  },
];

function cn(...classes) {
  return classes.filter(Boolean).join(" ");
}

function formatWon(value) {
  const numberValue = Number(String(value || "0").replace(/[^0-9]/g, ""));
  return numberValue.toLocaleString("ko-KR");
}

function UploadScreen({ onSelectFile }) {
  const fileInputRef = useRef(null);

  const handleClickUpload = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = (event) => {
    const file = event.target.files?.[0];
    if (!file) return;
    onSelectFile(file);
  };

  return (
    <div className="min-h-screen bg-slate-50 px-5 py-8 text-slate-950 md:px-10">
      <div className="mx-auto flex min-h-[calc(100vh-4rem)] max-w-7xl flex-col">
        <header className="mb-10 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-slate-950 text-white shadow-lg shadow-slate-200">
              <PiggyBank size={24} />
            </div>
            <div>
              <h1 className="text-xl font-black tracking-tight">TaxNow</h1>
              <p className="text-xs font-bold text-slate-400">
                실시간 절세 피드백 웹앱
              </p>
            </div>
          </div>

          <button className="hidden rounded-2xl bg-white px-4 py-3 text-sm font-bold text-slate-600 shadow-sm ring-1 ring-slate-200 md:block">
            Demo Mode
          </button>
        </header>

        <main className="grid flex-1 items-center gap-10 lg:grid-cols-[1.05fr_0.95fr]">
          <motion.section
            initial={{ opacity: 0, y: 18 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.45 }}
          >
            <div className="mb-5 inline-flex items-center gap-2 rounded-full bg-white px-4 py-2 text-sm font-bold text-slate-600 shadow-sm ring-1 ring-slate-100">
              <Sparkles size={17} />
              AI Tax Assistant
            </div>

            <h2 className="text-4xl font-black tracking-tight text-slate-950 md:text-6xl">
              거래내역을 넣으면
              <br />
              절세 피드백이 바로 나옵니다
            </h2>

            <p className="mt-6 max-w-2xl text-base leading-8 text-slate-500">
              거래내역 파일, 연말정산 간소화 파일, 카드 사용 내역 등을
              업로드하면 TaxNow AI 엔진이 공제 후보 항목과 절세 가능성을
              분석합니다.
            </p>

            <div className="mt-8 flex flex-col gap-3 sm:flex-row">
              <button
                onClick={handleClickUpload}
                className="flex items-center justify-center gap-2 rounded-2xl bg-slate-950 px-6 py-4 text-sm font-black text-white shadow-xl shadow-slate-200 transition hover:-translate-y-0.5 hover:shadow-2xl"
              >
                <UploadCloud size={19} />
                자료 업로드 시작
              </button>

              <button className="flex items-center justify-center gap-2 rounded-2xl bg-white px-6 py-4 text-sm font-black text-slate-700 shadow-sm ring-1 ring-slate-200 transition hover:bg-slate-100">
                <FileText size={19} />
                지원 파일 보기
              </button>
            </div>

            <input
              ref={fileInputRef}
              type="file"
              accept=".csv,.xlsx,.xls,.pdf,.txt"
              className="hidden"
              onChange={handleFileChange}
            />
          </motion.section>

          <motion.section
            initial={{ opacity: 0, scale: 0.96 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.45, delay: 0.1 }}
            className="rounded-[2rem] bg-white p-6 shadow-2xl shadow-slate-200 ring-1 ring-slate-100"
          >
            <div className="rounded-[1.5rem] bg-slate-950 p-6 text-white">
              <div className="mb-8 flex items-center justify-between">
                <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-white/10">
                  <Wallet size={27} />
                </div>
                <span className="rounded-full bg-white/10 px-4 py-2 text-xs font-bold text-slate-200">
                  예상 분석 화면
                </span>
              </div>

              <p className="text-sm font-bold text-slate-300">
                예상 절세 가능성
              </p>
              <p className="mt-2 text-4xl font-black">246,000원</p>
              <p className="mt-3 text-sm leading-6 text-slate-300">
                업로드된 거래내역과 예상 연봉을 기준으로 실시간 피드백을
                제공합니다.
              </p>
            </div>

            <div className="mt-5 space-y-3">
              {[
                ["거래내역 분석", "카드/계좌 소비 패턴 분류"],
                ["공제 항목 매핑", "의료비, 교통비, 연금 항목 확인"],
                ["실시간 피드백", "거래별 절세 가능성 안내"],
              ].map(([title, desc]) => (
                <div
                  key={title}
                  className="flex items-center gap-3 rounded-2xl bg-slate-50 p-4"
                >
                  <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-white text-emerald-600 shadow-sm">
                    <CheckCircle2 size={20} />
                  </div>
                  <div>
                    <p className="font-black text-slate-950">{title}</p>
                    <p className="text-sm text-slate-500">{desc}</p>
                  </div>
                </div>
              ))}
            </div>
          </motion.section>
        </main>
      </div>
    </div>
  );
}

function LoadingScreen({ selectedFile }) {
  return (
    <div className="flex min-h-screen items-center justify-center bg-[#0f172a] px-6 text-white">
      <motion.div
        initial={{ opacity: 0, y: 18 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.45 }}
        className="w-full max-w-xl text-center"
      >
        <div className="mx-auto mb-8 flex h-24 w-24 items-center justify-center rounded-[2rem] bg-white/10 shadow-2xl shadow-sky-950/40 ring-1 ring-white/10">
          <div className="h-14 w-14 animate-spin rounded-full border-4 border-white/20 border-t-white" />
        </div>

        <div className="mb-5 inline-flex items-center gap-2 rounded-full bg-white/10 px-4 py-2 text-sm font-bold text-slate-200 ring-1 ring-white/10">
          <Sparkles size={17} />
          TaxNow AI Engine
        </div>

        <h1 className="text-3xl font-black tracking-tight md:text-5xl">
          TaxNow AI 엔진이
          <br />
          거래 내역을 분석 중입니다...
        </h1>

        <p className="mt-5 text-sm leading-7 text-slate-300 md:text-base">
          업로드된 파일을 바탕으로 공제 후보 항목, 소비 패턴, 예상 절세
          가능성을 계산하고 있습니다.
        </p>

        {selectedFile && (
          <div className="mx-auto mt-8 max-w-md rounded-2xl bg-white/10 p-4 text-left ring-1 ring-white/10">
            <p className="text-xs font-bold text-slate-400">분석 중인 파일</p>
            <p className="mt-1 truncate text-sm font-black text-white">
              {selectedFile.name}
            </p>
            <p className="mt-1 text-xs text-slate-400">
              {(selectedFile.size / 1024).toFixed(1)} KB
            </p>
          </div>
        )}

        <div className="mt-8 h-2 overflow-hidden rounded-full bg-white/10">
          <motion.div
            initial={{ width: "0%" }}
            animate={{ width: "100%" }}
            transition={{ duration: 3, ease: "easeInOut" }}
            className="h-full rounded-full bg-white"
          />
        </div>
      </motion.div>
    </div>
  );
}

function Sidebar({ activeTab, setActiveTab, onReset, salary, previewData }) {
  const safePreview = previewData?.previewData || previewData;
  const totalDeduction = safePreview?.totalDeduction || 246000;

  return (
    <aside className="hidden h-screen w-72 shrink-0 border-r border-slate-200 bg-white px-5 py-6 lg:sticky lg:top-0 lg:block">
      <div className="flex items-center gap-3 px-2">
        <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-950 text-white shadow-lg shadow-slate-200">
          <PiggyBank size={23} />
        </div>
        <div>
          <h1 className="text-lg font-black tracking-tight text-slate-950">
            TaxNow
          </h1>
          <p className="text-xs font-semibold text-slate-400">
            절세 피드백 웹앱
          </p>
        </div>
      </div>

      <nav className="mt-9 space-y-1">
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = activeTab === item.id;

          return (
            <button
              key={item.id}
              onClick={() => setActiveTab(item.id)}
              className={cn(
                "flex w-full items-center gap-3 rounded-2xl px-4 py-3 text-sm font-bold transition",
                isActive
                  ? "bg-slate-950 text-white shadow-lg shadow-slate-200"
                  : "text-slate-500 hover:bg-slate-100 hover:text-slate-950"
              )}
            >
              <Icon size={19} />
              {item.fullLabel}
            </button>
          );
        })}
      </nav>

      <div className="absolute bottom-6 left-5 right-5 rounded-3xl bg-slate-950 p-5 text-white">
        <div className="mb-4 flex h-11 w-11 items-center justify-center rounded-2xl bg-white/10">
          <ShieldCheck size={22} />
        </div>
        <p className="text-sm font-bold">올해 절세 가능성</p>
        <p className="mt-2 text-2xl font-black">{formatWon(totalDeduction)}원</p>
        <p className="mt-2 text-xs leading-5 text-slate-300">
          예상 연봉 {formatWon(salary)}원 기준의 시연용 추정값입니다.
        </p>

        <button
          onClick={onReset}
          className="mt-4 flex w-full items-center justify-center gap-2 rounded-2xl bg-white/10 px-4 py-3 text-xs font-bold text-white transition hover:bg-white/15"
        >
          <RotateCcw size={15} />
          시연 다시 시작
        </button>
      </div>
    </aside>
  );
}

function MobileBottomNav({ activeTab, setActiveTab }) {
  return (
    <nav className="fixed bottom-0 left-0 right-0 z-30 border-t border-slate-200 bg-white/95 px-3 py-2 backdrop-blur lg:hidden">
      <div className="grid grid-cols-5 gap-1">
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = activeTab === item.id;

          return (
            <button
              key={item.id}
              onClick={() => setActiveTab(item.id)}
              className={cn(
                "flex flex-col items-center justify-center rounded-2xl px-2 py-2 text-[11px] font-bold transition",
                isActive ? "bg-slate-950 text-white" : "text-slate-400"
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

function Topbar({ activeTab }) {
  const titleMap = {
    home: "홈 대시보드",
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
            <p className="text-xs font-bold text-slate-400">
              TaxNow Web App
            </p>
            <h2 className="text-xl font-black text-slate-950 md:text-2xl">
              {titleMap[activeTab]}
            </h2>
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
        {Icon && <Icon size={22} />}
      </div>
      <p className="text-sm font-bold text-slate-400">{label}</p>
      <p className="mt-2 text-2xl font-black text-slate-950">{value || "0원"}</p>
      <p className="mt-2 text-xs leading-5 text-slate-500">{caption}</p>
    </div>
  );
}

function UploadedFileList({ selectedFile }) {
  const files = selectedFile
    ? [
        {
          id: 0,
          name: selectedFile.name,
          type: "사용자 업로드 파일",
          status: "분석 완료",
          size: `${(selectedFile.size / 1024).toFixed(1)}KB`,
        },
        ...defaultUploadedFiles,
      ]
    : defaultUploadedFiles;

  return (
    <section className="rounded-[1.75rem] bg-white p-6 shadow-sm ring-1 ring-slate-100">
      <div className="mb-5 flex items-center justify-between">
        <div>
          <p className="text-sm font-bold text-slate-400">업로드된 파일</p>
          <h3 className="text-xl font-black text-slate-950">자료 분석 상태</h3>
        </div>
        <span className="rounded-full bg-emerald-50 px-3 py-1 text-xs font-bold text-emerald-700">
          분석 완료
        </span>
      </div>

      <div className="space-y-3">
        {files.map((file) => (
          <div
            key={file.id}
            className="flex items-center justify-between rounded-2xl bg-slate-50 p-4"
          >
            <div className="flex min-w-0 items-center gap-3">
              <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-white text-slate-700 shadow-sm">
                <FileText size={20} />
              </div>
              <div className="min-w-0">
                <p className="truncate font-bold text-slate-950">
                  {file.name}
                </p>
                <p className="truncate text-xs text-slate-500">
                  {file.type} · {file.size}
                </p>
              </div>
            </div>

            <span className="ml-3 shrink-0 rounded-full bg-emerald-100 px-3 py-1 text-xs font-bold text-emerald-700">
              {file.status}
            </span>
          </div>
        ))}
      </div>
    </section>
  );
}

function FeedbackCard({ item }) {
  const styles = {
    good: "bg-emerald-50 text-emerald-700 ring-emerald-100",
    warning: "bg-amber-50 text-amber-700 ring-amber-100",
    tip: "bg-indigo-50 text-indigo-700 ring-indigo-100",
  };

  const cardType = item.severity || item.type || "good";
  const Icon = cardType === "warning" ? AlertTriangle : cardType === "tip" ? Sparkles : ShieldCheck;

  return (
    <motion.div
      initial={{ opacity: 0, y: 14 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35 }}
      className="rounded-[1.75rem] bg-white p-5 shadow-sm ring-1 ring-slate-100"
    >
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div className="flex gap-4">
          <div
            className={cn(
              "flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl ring-1",
              styles[cardType] || styles.good
            )}
          >
            <Icon size={22} />
          </div>

          <div>
            <div className="mb-2 flex flex-wrap items-center gap-2">
              <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-bold text-slate-600">
                {item.tag || "공제항목"}
              </span>
              <span className="text-xs font-bold text-slate-400">
                실시간 피드백
              </span>
            </div>

            <h4 className="font-black text-slate-950">{item.title}</h4>
            <p className="mt-2 text-sm leading-6 text-slate-500">
              {item.description}
            </p>
          </div>
        </div>

        <p className="w-fit shrink-0 rounded-full bg-slate-950 px-3 py-1 text-xs font-bold text-white">
          {item.amount || "분석 완료"}
        </p>
      </div>
    </motion.div>
  );
}

function HomeDashboard({ selectedFile, salary, previewData }) {
  const safePreview = previewData?.previewData || previewData;
  const totalDeduction = safePreview?.totalDeduction || 246000;

  return (
    <motion.div
      key="home"
      initial={{ opacity: 0, y: 18 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35 }}
      className="space-y-6"
    >
      <section className="rounded-[2rem] bg-slate-950 p-6 text-white shadow-2xl shadow-slate-200 md:p-8">
        <div className="grid gap-8 lg:grid-cols-[1.1fr_0.9fr] lg:items-center">
          <div>
            <div className="mb-5 inline-flex items-center gap-2 rounded-full bg-white/10 px-4 py-2 text-sm font-bold text-slate-200">
              <Sparkles size={17} />
              실시간 절세 어시스턴트
            </div>

            <h1 className="text-3xl font-black tracking-tight md:text-5xl">
              분석이 완료되었습니다
              <br />
              지금부터 거래별 피드백을 확인하세요
            </h1>

            <p className="mt-5 max-w-2xl text-sm leading-7 text-slate-300 md:text-base">
              업로드된 거래내역과 예상 연봉을 바탕으로 공제 후보 항목, 소비
              패턴, 예상 절세 가능성을 분석했습니다.
            </p>

            <div className="mt-6 flex flex-wrap gap-3">
              {selectedFile && (
                <div className="w-fit rounded-2xl bg-white/10 px-4 py-3 text-sm font-bold text-slate-200">
                  분석 파일: {selectedFile.name}
                </div>
              )}

              <div className="w-fit rounded-2xl bg-white/10 px-4 py-3 text-sm font-bold text-slate-200">
                예상 연봉: {formatWon(salary)}원
              </div>
            </div>
          </div>

          <div className="rounded-[1.75rem] bg-white p-5 text-slate-950">
            <p className="text-sm font-bold text-slate-400">
              이번 달 분석 요약
            </p>

            <div className="mt-5 space-y-4">
              {[
                ["공제 후보 거래", "18건"],
                ["체크카드 추천 비중", "높음"],
                ["연금/IRP 추가 검토", "필요"],
              ].map(([label, value]) => (
                <div
                  key={label}
                  className="flex items-center justify-between rounded-2xl bg-slate-50 p-4"
                >
                  <span className="text-sm font-bold text-slate-500">
                    {label}
                  </span>
                  <span className="text-lg font-black text-slate-950">
                    {value}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard
          icon={Wallet}
          label="예상 연봉"
          value={`${formatWon(salary)}원`}
          caption="사용자가 입력한 올해 예상 총급여"
        />
        <StatCard
          icon={ReceiptText}
          label="이번 달 소비"
          value="1,284,500원"
          caption="업로드된 거래내역 기준 시연용 추정값"
        />
        <StatCard
          icon={ShieldCheck}
          label="예상 절세 효과"
          value={`${formatWon(totalDeduction)}원`}
          caption="입력 자료 기반의 실시간 계산값"
        />
        <StatCard
          icon={Landmark}
          label="공제 후보 항목"
          value="5개"
          caption="카드, 의료비, 교통, 연금 항목 포함"
        />
      </section>
    </motion.div>
  );
}

function UploadDashboardPage({ selectedFile }) {
  return (
    <motion.div
      key="upload"
      initial={{ opacity: 0, y: 18 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35 }}
      className="space-y-6"
    >
      <UploadedFileList selectedFile={selectedFile} />

      <section className="rounded-[1.75rem] bg-white p-6 shadow-sm ring-1 ring-slate-100">
        <h3 className="text-xl font-black text-slate-950">
          파일 업로드 후 처리 흐름
        </h3>

        <div className="mt-5 grid gap-4 md:grid-cols-3">
          {[
            [
              "1",
              "거래 데이터 정리",
              "날짜, 금액, 결제수단, 거래처명을 표준화합니다.",
            ],
            [
              "2",
              "공제 항목 매핑",
              "의료비, 교통비, 카드 사용액 등 후보 항목을 분류합니다.",
            ],
            [
              "3",
              "피드백 생성",
              "예상 연봉과 함께 절세 가능성을 계산합니다.",
            ],
          ].map(([step, title, desc]) => (
            <div key={step} className="rounded-2xl bg-slate-50 p-5">
              <div className="mb-4 flex h-9 w-9 items-center justify-center rounded-xl bg-slate-950 text-sm font-black text-white">
                {step}
              </div>
              <p className="font-black text-slate-950">{title}</p>
              <p className="mt-2 text-sm leading-6 text-slate-500">{desc}</p>
            </div>
          ))}
        </div>
      </section>
    </motion.div>
  );
}

function SalaryDashboardPage({ salary, setSalary }) {
  const formattedSalary = formatWon(salary);

  const handleSalaryChange = (event) => {
    const onlyNumber = event.target.value.replace(/[^0-9]/g, "");
    setSalary(onlyNumber);
  };

  return (
    <motion.div
      key="salary"
      initial={{ opacity: 0, y: 18 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35 }}
      className="grid gap-6 xl:grid-cols-[1.1fr_0.9fr]"
    >
      <section className="rounded-[2rem] bg-slate-950 p-6 text-white shadow-2xl shadow-slate-200 md:p-8">
        <div className="mb-8 flex items-center justify-between">
          <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-white/10">
            <Wallet size={28} />
          </div>

          <span className="rounded-full bg-white/10 px-4 py-2 text-sm font-bold text-slate-200">
            예상 연봉 입력
          </span>
        </div>

        <h2 className="text-3xl font-black tracking-tight md:text-5xl">
          올해 예상 총급여를
          <br />
          입력해주세요
        </h2>

        <p className="mt-5 max-w-2xl text-sm leading-7 text-slate-300 md:text-base">
          예상 연봉은 카드 사용 공제 기준, 의료비 공제 가능성,
          연금저축·IRP 추천 한도 계산의 기준값으로 활용됩니다.
        </p>

        <div className="mt-8 rounded-[1.75rem] bg-white p-5 text-slate-950">
          <label className="text-sm font-bold text-slate-500">
            예상 연봉
          </label>

          <div className="mt-3 flex items-end gap-3">
            <input
              value={salary}
              onChange={handleSalaryChange}
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
        <h3 className="text-xl font-black text-slate-950">
          연봉 정보가 필요한 이유
        </h3>

        <div className="mt-5 space-y-4">
          {[
            [
              "카드 소득공제 기준 계산",
              "총급여 대비 카드 사용액 기준을 비교합니다.",
            ],
            [
              "의료비 공제 가능성 추정",
              "총급여 기준 일정 비율 초과 여부를 확인합니다.",
            ],
            [
              "연금저축·IRP 전략 확인",
              "납입 한도와 예상 절세 효과를 함께 검토합니다.",
            ],
            [
              "실시간 소비 피드백 보정",
              "거래별 피드백을 연봉 구간에 맞춰 조정합니다.",
            ],
          ].map(([title, desc]) => (
            <div
              key={title}
              className="flex items-start gap-3 rounded-2xl bg-slate-50 p-4"
            >
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

function RecentTransactionCard({ transactions }) {
  const safeTransactions = transactions && transactions.length > 0 ? transactions : fallbackTransactions;

  return (
    <section className="rounded-[2rem] bg-white p-6 shadow-sm ring-1 ring-slate-100">
      <div className="mb-6 flex items-center justify-between">
        <div>
          <p className="text-sm font-bold text-slate-400">최근 거래</p>
          <h3 className="text-xl font-black text-slate-950">
            거래별 절세 인사이트
          </h3>
        </div>
        <BellRing className="text-slate-500" size={22} />
      </div>

      <div className="space-y-3">
        {safeTransactions.map((transaction, idx) => (
          <div
            key={idx}
            className="flex items-center justify-between gap-4 rounded-2xl bg-slate-50 p-4"
          >
            <div className="flex min-w-0 items-center gap-3">
              <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-white text-slate-700 shadow-sm">
                <CreditCard size={19} />
              </div>

              <div className="min-w-0">
                <p className="truncate font-black text-slate-950">
                  {transaction.merchantName || transaction.title || "지출 내역"}
                </p>
                <p className="truncate text-xs text-slate-500">
                  {transaction.category || "일반"} · {transaction.approvedAt || transaction.method || "최근"}
                </p>
              </div>
            </div>

            <div className="shrink-0 text-right">
              <p className="font-black text-slate-950">
                {typeof transaction.amount === "number" ? `${formatWon(transaction.amount)}원` : transaction.amount}
              </p>
              <p className="mt-1 text-xs font-bold text-emerald-600">
                {transaction.insight || "공제 반영 후보"}
              </p>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}

function AssistantFeedbackPreview({ realFeedbacks }) { 
  const safeFeedbacks = realFeedbacks && realFeedbacks.length > 0 ? realFeedbacks : fallbackFeedbacks;

  return (
    <section className="space-y-4">
      <div className="rounded-[2rem] bg-white p-6 shadow-sm ring-1 ring-slate-100">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm font-bold text-slate-400">TaxNow AI</p>
            <h3 className="text-xl font-black text-slate-950">
              실시간 절세 어시스턴트 알림
            </h3>
          </div>
          <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-slate-950 text-white">
            <Sparkles size={22} />
          </div>
        </div>
      </div>
      {safeFeedbacks.map((item, idx) => (
        <FeedbackCard key={idx} item={item} />
      ))}
    </section>
  );
}

function FeedbackDashboardPage({ transactions, feedbacks }) {
  return (
    <motion.div
      key="feedback"
      initial={{ opacity: 0, y: 18 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35 }}
      className="grid gap-6 xl:grid-cols-[0.95fr_1.05fr]"
    >
      <RecentTransactionCard transactions={transactions} />
      <AssistantFeedbackPreview realFeedbacks={feedbacks} />
    </motion.div>
  );
}

function ReportDashboardPage({ salary, previewData }) {
  const formattedSalary = formatWon(salary);
  const safePreview = previewData?.previewData || previewData;
  const progressPercent = safePreview?.progressPercent || 65;
  const totalDeduction = safePreview?.totalDeduction || 246000;

  return (
    <motion.div
      key="report"
      initial={{ opacity: 0, y: 18 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35 }}
      className="space-y-6"
    >
      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard
          icon={Wallet}
          label="예상 연봉"
          value={`${formattedSalary}원`}
          caption="리포트 계산에 반영되는 예상 총급여"
        />
        <StatCard
          icon={ShieldCheck}
          label="예상 절세"
          value={`${formatWon(totalDeduction)}원`}
          caption="현재 입력 자료 기준의 참고용 추정값"
        />
        <StatCard
          icon={CreditCard}
          label="카드 공제 후보"
          value="18건"
          caption="거래내역에서 분류된 후보 항목"
        />
        <StatCard
          icon={Landmark}
          label="추가 검토 항목"
          value="3개"
          caption="연금, 의료비, 교통비 항목"
        />
      </section>

      <section className="rounded-[2rem] bg-white p-6 shadow-sm ring-1 ring-slate-100">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="text-sm font-bold text-slate-400">
              연말정산 리포트
            </p>
            <h3 className="text-xl font-black text-slate-950">
              공제 항목별 진행률
            </h3>
          </div>

          <button className="w-fit rounded-2xl bg-slate-950 px-4 py-3 text-sm font-black text-white">
            리포트 내보내기
          </button>
        </div>

        <div className="mt-6 space-y-5">
          {[
            ["카드 사용액", `${progressPercent}%`],
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
                <div
                  className="h-3 rounded-full bg-slate-950"
                  style={{ width: percent }}
                />
              </div>
            </div>
          ))}
        </div>
      </section>

      <section className="rounded-[2rem] bg-white p-6 shadow-sm ring-1 ring-slate-100">
        <h3 className="text-xl font-black text-slate-950">다음 추천 액션</h3>

        <div className="mt-5 grid gap-4 md:grid-cols-3">
          {[
            [
              "체크카드 비중 조정",
              "남은 기간 소비 결제수단을 일부 조정해볼 수 있습니다.",
            ],
            [
              "연금저축 납입 확인",
              "기존 납입액 기준으로 추가 납입 여부를 검토합니다.",
            ],
            [
              "의료비 영수증 확인",
              "누락된 의료비 자료가 있는지 한 번 더 확인합니다.",
            ],
          ].map(([title, desc]) => (
            <div
              key={title}
              className="flex items-start justify-between gap-4 rounded-2xl bg-slate-50 p-5"
            >
              <div>
                <p className="font-black text-slate-950">{title}</p>
                <p className="mt-2 text-sm leading-6 text-slate-500">
                  {desc}
                </p>
              </div>
              <ChevronRight className="mt-1 shrink-0 text-slate-400" />
            </div>
          ))}
        </div>
      </section>
    </motion.div>
  );
}

function DashboardScreen({ selectedFile, onReset, transactions, feedbacks, previewData, isRefreshing }) { 
  const [activeTab, setActiveTab] = useState("home");
  const [salary, setSalary] = useState("42000000");

  const safeTransactions = transactions && transactions.length > 0 ? transactions : fallbackTransactions;
  const safeFeedbacks = feedbacks && feedbacks.length > 0 ? feedbacks : fallbackFeedbacks;
  const totalDeduction = previewData?.totalDeduction || 246000;

  const renderDashboardPage = () => {
    if (activeTab === "home") {
      return <HomeDashboard selectedFile={selectedFile} salary={salary} previewData={previewData} />;
    }
    if (activeTab === "upload") {
      return <UploadDashboardPage selectedFile={selectedFile} />;
    }
    if (activeTab === "salary") {
      return <SalaryDashboardPage salary={salary} setSalary={setSalary} />;
    }
    if (activeTab === "feedback") {
      return <FeedbackDashboardPage transactions={safeTransactions} feedbacks={safeFeedbacks} />;
    }
    if (activeTab === "report") {
      return <ReportDashboardPage salary={salary} previewData={previewData} />;
    }
    return <HomeDashboard selectedFile={selectedFile} salary={salary} previewData={previewData} />;
  };

  return (
    <div className="min-h-screen bg-slate-50 text-slate-950">
      {isRefreshing && (
        <div className="bg-slate-900 text-white text-xs font-bold py-2 text-center sticky top-0 z-50 flex items-center justify-center gap-2">
          <div className="h-3 w-3 animate-spin rounded-full border-2 border-white/20 border-t-white" />
          실시간 분석 데이터 업데이트 중...
        </div>
      )}

      <div className="flex min-h-screen">
        <Sidebar activeTab={activeTab} setActiveTab={setActiveTab} onReset={onReset} salary={salary} previewData={previewData} />
        <div className="min-w-0 flex-1 pb-24 lg:pb-0">
          <Topbar activeTab={activeTab} />
          <main className="mx-auto max-w-7xl px-4 py-6 md:px-8 md:py-8">
            

            {activeTab === "home" || activeTab === "feedback" ? (
              <div className="space-y-6">
                {activeTab === "home" && <HomeDashboard selectedFile={selectedFile} salary={salary} previewData={previewData} />}
                <section className="grid gap-6 xl:grid-cols-[0.95fr_1.05fr]">
                  {activeTab === "home" && <RecentTransactionCard transactions={safeTransactions} />}
                  <AssistantFeedbackPreview realFeedbacks={safeFeedbacks} />
                </section>
              </div>
            ) : (
              renderDashboardPage()
            )}

            <div className="mt-6 pb-6 lg:hidden">
              <button onClick={onReset} className="flex w-full items-center justify-center gap-2 rounded-2xl bg-slate-950 px-5 py-4 text-sm font-black text-white">
                <RotateCcw size={18} /> 시연 다시 시작
              </button>
            </div>
          </main>
        </div>
      </div>
      <MobileBottomNav activeTab={activeTab} setActiveTab={setActiveTab} />
    </div>
  );
}

export default function App() {
  const [screen, setScreen] = useState("UPLOAD");
  const [selectedFile, setSelectedFile] = useState(null);

  const [transactions, setTransactions] = useState([]);
  const [feedbacks, setFeedbacks] = useState([]);
  const [previewData, setPreviewData] = useState(null); 

  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [error, setError] = useState(null);

  const handleSelectFile = (file) => {
    setSelectedFile(file);
    setScreen("LOADING");
  };

  const handleResetDemo = () => {
    setSelectedFile(null);
    setScreen("UPLOAD");
    setTransactions([]);
    setFeedbacks([]);
    setPreviewData(null);
  };

  useEffect(() => {
    if (screen !== "DASHBOARD") {
      if (screen === "LOADING") {
        const timer = setTimeout(() => setScreen("DASHBOARD"), 3000);
        return () => clearTimeout(timer);
      }
      return;
    }

    const syncTaxData = async () => {
    try {
      setIsRefreshing(true);
      
      await fetch(`${API_BASE}/api/sync`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userId: FIXED_USER_ID,
          connectedId: "test_connected_id",
          organization: "test_org"
        })
      }).then(res => res.json());

      const txData = await fetch(`${API_BASE}/api/mock/transactions?userId=${FIXED_USER_ID}`).then(res => res.json());
      setTransactions(txData);

      const fbData = await fetch(`${API_BASE}/api/users/${FIXED_USER_ID}/feedbacks`).then(res => res.json());
      setFeedbacks(fbData);

      const pvData = await fetch(`${API_BASE}/api/deduction-engine/users/${FIXED_USER_ID}/preview`).then(res => res.json());
      setPreviewData(pvData);

    } catch (err) {
      console.error("❌ 실시간 연동 중 에러 발생:", err);
      setError(err.message);
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

    syncTaxData();
  }, [screen]);

  if (screen === "UPLOAD") {
    return <UploadScreen onSelectFile={handleSelectFile} />;
  }

  if (screen === "LOADING") {
    return <LoadingScreen selectedFile={selectedFile} />;
  }

  return (
    <DashboardScreen 
      selectedFile={selectedFile} 
      onReset={handleResetDemo} 
      transactions={transactions}
      feedbacks={feedbacks}
      previewData={previewData}
      isRefreshing={isRefreshing}
    />
  );
}