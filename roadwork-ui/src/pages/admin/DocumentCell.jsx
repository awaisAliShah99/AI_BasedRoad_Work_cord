// Admin's "Document" column: photo thumbnail, status badge and Verify / Reject buttons
import { useState } from 'react'
import { verifyDocument } from '../../api/adminApi'

const BASE = 'http://localhost:9090'

const COLORS = {
    PENDING:  { background: '#fef3c7', color: '#92400e' },
    VERIFIED: { background: '#d1fae5', color: '#065f46' },
    REJECTED: { background: '#fee2e2', color: '#991b1b' },
}

function DocumentCell({ request, onChanged }) {
    const [saving, setSaving] = useState(false)

    if (!request.documentUrl) {
        return <span style={{ color: '#9ca3af' }}>—</span>
    }

    const status = request.documentStatus || 'PENDING'

    const act = async (newStatus) => {
        let remarks = ''
        if (newStatus === 'REJECTED') {
            remarks = window.prompt('Reason for rejecting this document:')
            if (!remarks || !remarks.trim()) return
        }
        setSaving(true)
        try {
            await verifyDocument(request.id, newStatus, remarks)
            onChanged()
        } catch (err) {
            alert(err.response?.data?.message || 'Failed to update document status')
        } finally {
            setSaving(false)
        }
    }

    return (
        <div>
            <a href={`${BASE}${request.documentUrl}`} target="_blank" rel="noreferrer">
                <img
                    src={`${BASE}${request.documentUrl}`}
                    alt="document"
                    style={{ width: 48, height: 48, objectFit: 'cover', borderRadius: 6 }}
                />
            </a>

            <div style={{ ...s.badge, ...COLORS[status] }}>{status}</div>

            {status === 'REJECTED' && request.documentRemarks && (
                <div style={s.remarks}>{request.documentRemarks}</div>
            )}

            {status === 'PENDING' && (
                <div style={s.actions}>
                    <button style={s.verify} disabled={saving} onClick={() => act('VERIFIED')}>Verify</button>
                    <button style={s.reject} disabled={saving} onClick={() => act('REJECTED')}>Reject</button>
                </div>
            )}
        </div>
    )
}

const s = {
    badge: { display: 'inline-block', marginTop: '0.3rem', padding: '0.1rem 0.5rem', borderRadius: '999px', fontSize: '0.65rem', fontWeight: '700' },
    remarks: { fontSize: '0.72rem', color: '#6b7280', marginTop: '0.25rem', maxWidth: '150px' },
    actions: { display: 'flex', gap: '0.3rem', marginTop: '0.35rem' },
    verify: { padding: '0.2rem 0.5rem', background: '#16a34a', color: '#fff', border: 'none', borderRadius: '5px', fontSize: '0.7rem', fontWeight: '600', cursor: 'pointer' },
    reject: { padding: '0.2rem 0.5rem', background: '#dc2626', color: '#fff', border: 'none', borderRadius: '5px', fontSize: '0.7rem', fontWeight: '600', cursor: 'pointer' },
}

export default DocumentCell