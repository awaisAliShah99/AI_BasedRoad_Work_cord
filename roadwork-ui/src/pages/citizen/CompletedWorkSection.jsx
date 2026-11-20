// Citizen page: finished jobs with their rating. Citizens can rate each job once.
import { useState, useEffect } from 'react'
import { useAuth } from '../../context/AuthContext'
import { getCompletedWork, submitRating } from '../../api/ratingApi'

function RateModal({ work, onClose, onDone }) {
    const [score, setScore] = useState(0)
    const [hover, setHover] = useState(0)
    const [comment, setComment] = useState('')
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    const handleSubmit = async (e) => {
        e.preventDefault()
        if (score < 1) {
            setError('Please select a star rating')
            return
        }
        setLoading(true)
        setError('')
        try {
            await submitRating(work.id, { score, comment })
            onDone()
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to submit rating')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div style={s.overlay} onClick={onClose}>
            <div style={s.modal} onClick={(e) => e.stopPropagation()}>
                <h3 style={s.modalTitle}>Rate this work</h3>
                <div style={s.sub}>{work.roadSegmentName} · {work.departmentName}</div>

                {error && <div style={s.error}>{error}</div>}

                <form onSubmit={handleSubmit}>
                    <div style={s.stars}>
                        {[1, 2, 3, 4, 5].map(n => (
                            <span
                                key={n}
                                style={{ ...s.star, color: n <= (hover || score) ? '#f59e0b' : '#d1d5db' }}
                                onMouseEnter={() => setHover(n)}
                                onMouseLeave={() => setHover(0)}
                                onClick={() => setScore(n)}
                            >
                ★
              </span>
                        ))}
                    </div>

                    <textarea
                        style={s.textarea}
                        value={comment}
                        onChange={(e) => setComment(e.target.value)}
                        placeholder="How was the work: quality, time taken, cleanup... (optional)"
                    />

                    <div style={s.actions}>
                        <button type="button" style={s.cancelBtn} onClick={onClose}>Cancel</button>
                        <button type="submit" style={s.submitBtn} disabled={loading}>
                            {loading ? 'Submitting...' : 'Submit'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    )
}

function CompletedWorkSection() {
    const { user } = useAuth()
    const [work, setWork] = useState([])
    const [loading, setLoading] = useState(true)
    const [target, setTarget] = useState(null)
    const [thanks, setThanks] = useState(false)

    const load = () =>
        getCompletedWork()
            .then(res => setWork(res.data.data || []))
            .catch(() => setWork([]))
            .finally(() => setLoading(false))

    useEffect(() => { load() }, [])

    const isCitizen = user?.role === 'CITIZEN'

    const handleDone = () => {
        setTarget(null)
        setThanks(true)
        setTimeout(() => setThanks(false), 4000)
        load()
    }

    return (
        <div style={s.section}>
            <h3 style={s.title}>Completed work{isCitizen ? ' — rate it' : ''}</h3>

            {thanks && <div style={s.thanks}>Thank you for your feedback!</div>}

            {loading ? (
                <div style={s.empty}>Loading...</div>
            ) : work.length === 0 ? (
                <div style={s.empty}>No completed work yet</div>
            ) : (
                <div style={s.grid}>
                    {work.map(w => (
                        <div key={w.id} style={s.card}>
                            <div style={s.cardTitle}>{w.roadSegmentName}</div>
                            <div style={s.city}>{w.city}</div>
                            <div style={s.row}>{w.departmentName} · {w.workType?.replace(/_/g, ' ')}</div>
                            <div style={s.row}>Finished: {w.endDate}</div>
                            <div style={s.rating}>
                                {w.averageRating != null
                                    ? <>★ {w.averageRating} <span style={s.count}>({w.ratingCount})</span></>
                                    : <span style={s.count}>Not rated yet</span>}
                            </div>

                            {isCitizen && (
                                w.ratedByMe
                                    ? <div style={s.done}>You rated this</div>
                                    : <button style={s.rateBtn} onClick={() => setTarget(w)}>Rate this work</button>
                            )}
                        </div>
                    ))}
                </div>
            )}

            {target && (
                <RateModal work={target} onClose={() => setTarget(null)} onDone={handleDone} />
            )}
        </div>
    )
}

const s = {
    section: { marginTop: '1.75rem' },
    title: { fontSize: '1.1rem', fontWeight: '700', color: '#1a1a2e', marginBottom: '1rem' },
    empty: { background: '#fff', borderRadius: '12px', padding: '2rem', textAlign: 'center', color: '#9ca3af', border: '1px dashed #e5e7eb' },
    thanks: { background: '#d1fae5', color: '#065f46', padding: '0.65rem 1rem', borderRadius: '8px', marginBottom: '1rem', fontSize: '0.85rem' },
    grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))', gap: '1rem' },
    card: { background: '#fff', border: '1px solid #e5e7eb', borderRadius: '12px', padding: '1rem' },
    cardTitle: { fontWeight: '700', fontSize: '0.95rem', color: '#1a1a2e' },
    city: { fontSize: '0.75rem', color: '#9ca3af', marginBottom: '0.5rem' },
    row: { fontSize: '0.8rem', color: '#374151', marginTop: '0.2rem' },
    rating: { marginTop: '0.6rem', fontWeight: '700', color: '#d97706' },
    count: { fontWeight: '400', color: '#6b7280', fontSize: '0.8rem' },
    rateBtn: { marginTop: '0.75rem', width: '100%', padding: '0.55rem', background: '#2563eb', color: '#fff', border: 'none', borderRadius: '8px', fontWeight: '600', cursor: 'pointer' },
    done: { marginTop: '0.75rem', fontSize: '0.8rem', color: '#065f46', fontWeight: '600' },
    overlay: { position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '1rem' },
    modal: { background: '#fff', borderRadius: '12px', padding: '1.5rem', width: '100%', maxWidth: '400px' },
    modalTitle: { fontSize: '1.1rem', fontWeight: '700', color: '#1a1a2e', margin: 0 },
    sub: { fontSize: '0.8rem', color: '#6b7280', margin: '0.25rem 0 1rem' },
    error: { background: '#fee2e2', color: '#991b1b', padding: '0.6rem 1rem', borderRadius: '8px', marginBottom: '1rem', fontSize: '0.85rem' },
    stars: { display: 'flex', gap: '0.35rem', marginBottom: '1rem' },
    star: { fontSize: '2rem', cursor: 'pointer', lineHeight: 1, userSelect: 'none' },
    textarea: { width: '100%', minHeight: '90px', padding: '0.6rem 0.8rem', border: '1px solid #d1d5db', borderRadius: '8px', fontSize: '0.9rem', boxSizing: 'border-box', fontFamily: 'inherit', resize: 'vertical' },
    actions: { display: 'flex', gap: '0.75rem', marginTop: '1.25rem' },
    cancelBtn: { flex: 1, padding: '0.65rem', background: '#f3f4f6', color: '#374151', border: 'none', borderRadius: '8px', fontWeight: '600', cursor: 'pointer' },
    submitBtn: { flex: 1, padding: '0.65rem', background: '#2563eb', color: '#fff', border: 'none', borderRadius: '8px', fontWeight: '600', cursor: 'pointer' },
}

export default CompletedWorkSection