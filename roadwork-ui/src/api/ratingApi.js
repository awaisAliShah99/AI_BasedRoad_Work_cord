import api from './axiosConfig'

export const getCompletedWork = () => api.get('/api/completed-work')
export const submitRating = (workRequestId, data) =>
    api.post(`/api/citizen/ratings/${workRequestId}`, data)
export const getAllRatings = () => api.get('/api/admin/ratings')