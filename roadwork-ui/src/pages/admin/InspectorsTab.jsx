import { useState, useEffect } from 'react'
import { getInspectors, createInspector } from '../../api/inspectionApi'

const EMPTY = { fullName: '', email: '', password: '' }

function InspectorsTab() {
    const [inspectors, setInspectors] = useState([])
    const [loading, setLoading] = useState(true)
    const [form, setForm] = useState(EMPTY)
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')
    const [submitting, setSubmitting] = useState(false)

    const load = () =>
        getInspectors()
            .then(res => setInspectors(res.data.data || []))
            .finally(() => setLoading(false))

    useEffect(() => { load() }, [])

    const handleSubmit = async (e) => {
        e.preventDefault()
        setError('')
        setSuccess('')

        if (!form.fullName || !form.email || !form.password) {
            setError('Name, email and password are required')
            return
        }

        setSubmitting(true)
        try {
            await createInspector(form)
            setSuccess(`Inspector account created for ${form.email}`)
            setForm(EMPTY)
            await load()
        } catch (err) {
            const data = err.response?.data
            // Validation errors may come back as a list of field messages
            setError(data?.message || 'Failed to create inspector')
        } finally {
            setSubmitting(false)
        }
    }

    return (
        <div>
            <form onSubmit={handleSubmit} style={s.formCard}>
                <h4 style={s.formTitle}>Create Inspector Account</h4>
                {error && <div style={s.error}>{error}</div>}
                {success && <div style={s.success}>{success}</div>}
                <div style={s.row}>
                    <input style={s.input} placeholder="Full name" value={form.fullName}
                           onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
                    <input style={s.input} type="email" placeholder="Email" value={form.email}
                           onChange={(e) => setForm({ ...form, email: e.target.value })} />
                    <input style={s.input} type="password" placeholder="Password (min 6 characters)" value={form.password}
                           onChange={(e) => setForm({ ...form, password: e.target.value })} />
                    <button type="submit" style={s.btn} disabled={submitting}>
                        {submitting ? 'Creating...' : 'Create'}
                    </button>
                </div>
                <div style={s.hint}>
                    Share the email and password with the inspector. They log in from the normal login page.
                </div>
            </form>

            {loading ? (
                <div style={s.empty}>Loading...</div>
            ) : inspectors.length === 0 ? (
                <div style={s.empty}>No inspectors yet</div>
            ) : (
                <div style={s.tableWrapper}>
                    <table style={s.table}>
                        <thead>
                        <tr>
                            <th style={s.th}>Name</th>
                            <th style={s.th}>Email</th>
                        </tr>
                        </thead>
                        <tbody>
                        {inspectors.map(i => (
                            <tr key={i.id} style={s.tr}>
                                <td style={s.td}>{i.fullName}</td>
                                <td style={s.td}>{i.email}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    )
}

const s = {
    formCard: { background: '#fff', border: '1px solid #e5e7eb', borderRadius: '10px', padding: '1.25rem', marginBottom: '1.5rem' },
    formTitle: { fontSize: '0.95rem', fontWeight: '600', color: '#374151', margin: 0 },
    row: { display: 'flex', gap: '0.75rem', flexWrap: 'wrap', marginTop: '0.75rem' },
    input: { flex: 1, minWidth: '170px', padding: '0.6rem 0.8rem', border: '1px solid #d1d5db', borderRadius: '8px', fontSize: '0.9rem', outline: 'none' },
    btn: { padding: '0.6rem 1.3rem', background: '#1a1a2e', color: '#fff', border: 'none', borderRadius: '8px', fontWeight: '600', cursor: 'pointer' },
    hint: { fontSize: '0.78rem', color: '#6b7280', marginTop: '0.6rem' },
    error: { background: '#fee2e2', color: '#991b1b', padding: '0.6rem 1rem', borderRadius: '8px', marginTop: '0.75rem', fontSize: '0.85rem' },
    success: { background: '#d1fae5', color: '#065f46', padding: '0.6rem 1rem', borderRadius: '8px', marginTop: '0.75rem', fontSize: '0.85rem' },
    empty: { textAlign: 'center', padding: '3rem', color: '#6b7280' },
    tableWrapper: { background: '#fff', border: '1px solid #e5e7eb', borderRadius: '10px', overflowX: 'auto' },
    table: { width: '100%', borderCollapse: 'collapse' },
    th: { textAlign: 'left', padding: '0.85rem 1.25rem', fontSize: '0.8rem', color: '#6b7280', borderBottom: '1px solid #e5e7eb' },
    tr: { borderBottom: '1px solid #f3f4f6' },
    td: { padding: '0.9rem 1.25rem', fontSize: '0.875rem', color: '#1f2937' },
}

export default InspectorsTab