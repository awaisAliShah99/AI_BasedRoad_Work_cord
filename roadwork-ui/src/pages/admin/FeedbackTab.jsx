import { useState, useEffect } from 'react'
import { getAllRatings } from '../../api/ratingApi'

const stars = (n) => '★'.repeat(n) + '☆'.repeat(5 - n)

function FeedbackTab() {
    const [ratings, setRatings] = useState([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        getAllRatings()
            .then(res => setRatings(res.data.data || []))
            .finally(() => setLoading(false))
    }, [])

    if (loading) return <div style={s.empty}>Loading...</div>
    if (ratings.length === 0) return <div style={s.empty}>No citizen feedback yet</div>

    const average = (ratings.reduce((sum, r) => sum + r.score, 0) / ratings.length).toFixed(1)

    return (
        <div>
            <div style={s.summary}>
                Average rating: <strong>★ {average}</strong> from {ratings.length} review{ratings.length === 1 ? '' : 's'}
            </div>
            <div style={s.tableWrapper}>
                <table style={s.table}>
                    <thead>
                    <tr>
                        <th style={s.th}>Date</th>
                        <th style={s.th}>Department</th>
                        <th style={s.th}>Road Segment</th>
                        <th style={s.th}>Citizen</th>
                        <th style={s.th}>Rating</th>
                        <th style={s.th}>Comment</th>
                    </tr>
                    </thead>
                    <tbody>
                    {ratings.map(r => (
                        <tr key={r.id} style={s.tr}>
                            <td style={s.td}>{new Date(r.createdAt).toLocaleDateString()}</td>
                            <td style={s.td}>{r.departmentName}</td>
                            <td style={s.td}>{r.roadSegmentName}</td>
                            <td style={s.td}>{r.raterName}</td>
                            <td style={{ ...s.td, color: '#d97706', whiteSpace: 'nowrap' }}>{stars(r.score)}</td>
                            <td style={s.td}>{r.comment || '—'}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    )
}

const s = {
    empty: { background: '#fff', borderRadius: '12px', padding: '3rem', textAlign: 'center', color: '#9ca3af', border: '1px dashed #e5e7eb' },
    summary: { marginBottom: '1rem', fontSize: '0.9rem', color: '#374151' },
    tableWrapper: { background: '#fff', borderRadius: '12px', overflowX: 'auto', border: '1px solid #e5e7eb' },
    table: { width: '100%', borderCollapse: 'collapse' },
    th: { textAlign: 'left', padding: '0.85rem 1.1rem', fontSize: '0.75rem', fontWeight: '600', color: '#6b7280', textTransform: 'uppercase', background: '#f9fafb', borderBottom: '1px solid #e5e7eb' },
    tr: { borderBottom: '1px solid #f3f4f6' },
    td: { padding: '0.9rem 1.1rem', fontSize: '0.85rem', color: '#1f2937', verticalAlign: 'top' },
}

export default FeedbackTab