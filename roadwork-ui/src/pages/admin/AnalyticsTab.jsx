import { useState, useEffect } from 'react'
import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
    PieChart, Pie, Cell, Legend,
} from 'recharts'
import { getAnalytics, downloadSummaryReport } from '../../api/analyticsApi'

const clean = (rows = []) => rows.map(r => ({ name: r.label.replace(/_/g, ' '), count: r.count }))

const VERDICT_COLORS = {
    'RESTORED': '#16a34a',
    'PARTIALLY RESTORED': '#d97706',
    'NOT RESTORED': '#dc2626',
}

function BarCard({ title, data, color }) {
    return (
        <div style={s.chartCard}>
            <h4 style={s.chartTitle}>{title}</h4>
            <ResponsiveContainer width="100%" height={260}>
                <BarChart data={data} margin={{ top: 5, right: 10, left: -15, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" tick={{ fontSize: 10 }} interval={0} angle={-25} textAnchor="end" height={70} />
                    <YAxis allowDecimals={false} />
                    <Tooltip />
                    <Bar dataKey="count" fill={color} radius={[4, 4, 0, 0]} />
                </BarChart>
            </ResponsiveContainer>
        </div>
    )
}

function AnalyticsTab() {
    const [data, setData] = useState(null)
    const [loading, setLoading] = useState(true)
    const [downloading, setDownloading] = useState(false)

    useEffect(() => {
        getAnalytics()
            .then(res => setData(res.data.data))
            .finally(() => setLoading(false))
    }, [])

    const handleDownload = async () => {
        setDownloading(true)
        try {
            const res = await downloadSummaryReport()
            const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }))
            const link = document.createElement('a')
            link.href = url
            link.download = 'road-work-summary-report.pdf'
            document.body.appendChild(link)
            link.click()
            link.remove()
            window.URL.revokeObjectURL(url)
        } catch {
            alert('Failed to download report')
        } finally {
            setDownloading(false)
        }
    }

    if (loading) return <div style={s.empty}>Loading...</div>
    if (!data) return <div style={s.empty}>Could not load analytics</div>

    const cards = [
        ['Total Requests', data.totalRequests],
        ['Pending', data.pendingRequests],
        ['Approved', data.approvedRequests],
        ['In Progress', data.inProgressRequests],
        ['Completed', data.completedRequests],
        ['Rejected', data.rejectedRequests],
        ['Conflicts', data.totalConflicts],
        ['Unresolved Conflicts', data.pendingConflicts],
        ['Complaints', data.totalComplaints],
        ['Open Complaints', data.openComplaints],
        ['Inspections', data.totalInspections],
    ]

    const verdicts = clean(data.inspectionsByVerdict)
    const hasInspections = verdicts.some(v => v.count > 0)

    return (
        <div>
            <div style={s.header}>
                <h3 style={s.heading}>System overview</h3>
                <button style={s.pdfBtn} onClick={handleDownload} disabled={downloading}>
                    {downloading ? 'Preparing...' : 'Download PDF Report'}
                </button>
            </div>

            <div style={s.cards}>
                {cards.map(([label, value]) => (
                    <div key={label} style={s.card}>
                        <div style={s.cardValue}>{value}</div>
                        <div style={s.cardLabel}>{label}</div>
                    </div>
                ))}
            </div>

            <div style={s.charts}>
                <BarCard title="Requests by status" data={clean(data.requestsByStatus)} color="#2563eb" />
                <BarCard title="Requests by work type" data={clean(data.requestsByWorkType)} color="#16a34a" />
                <BarCard title="Complaints by category" data={clean(data.complaintsByCategory)} color="#d97706" />

                <div style={s.chartCard}>
                    <h4 style={s.chartTitle}>Inspection verdicts</h4>
                    {hasInspections ? (
                        <ResponsiveContainer width="100%" height={260}>
                            <PieChart>
                                <Pie data={verdicts} dataKey="count" nameKey="name" outerRadius={85} label>
                                    {verdicts.map(v => (
                                        <Cell key={v.name} fill={VERDICT_COLORS[v.name] || '#9ca3af'} />
                                    ))}
                                </Pie>
                                <Tooltip />
                                <Legend />
                            </PieChart>
                        </ResponsiveContainer>
                    ) : (
                        <div style={{ ...s.empty, border: 'none' }}>No inspection reports yet</div>
                    )}
                </div>
            </div>
        </div>
    )
}

const s = {
    empty: { background: '#fff', borderRadius: '12px', padding: '3rem', textAlign: 'center', color: '#9ca3af', border: '1px dashed #e5e7eb' },
    header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' },
    heading: { fontSize: '1rem', fontWeight: '700', color: '#1a1a2e', margin: 0 },
    pdfBtn: { padding: '0.6rem 1.1rem', background: '#1a1a2e', color: '#fff', border: 'none', borderRadius: '8px', fontSize: '0.85rem', fontWeight: '600', cursor: 'pointer' },
    cards: { display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '1rem', marginBottom: '1.75rem' },
    card: { background: '#fff', border: '1px solid #e5e7eb', borderRadius: '12px', padding: '1rem', textAlign: 'center' },
    cardValue: { fontSize: '1.6rem', fontWeight: '700', color: '#1a1a2e' },
    cardLabel: { fontSize: '0.78rem', color: '#6b7280', marginTop: '0.25rem' },
    charts: { display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: '1.25rem' },
    chartCard: { background: '#fff', border: '1px solid #e5e7eb', borderRadius: '12px', padding: '1rem' },
    chartTitle: { fontSize: '0.9rem', fontWeight: '600', color: '#374151', margin: '0 0 0.75rem' },
}

export default AnalyticsTab