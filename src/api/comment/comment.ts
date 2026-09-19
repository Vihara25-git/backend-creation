import apiClient from '../../lib/api';

export interface Comment {
  id: number;
  comment: string;
  userId?: number | string;
  defectId?: number | string;
  attachment?: string | null;
  createdAt?: string;
  createdByName?: string;
}

export interface GetCommentsResponse {
  message: string;
  data: Comment[];
  status?: string;
  statusCode?: number;
}

export const getCommentsByDefectId = async (defectId: number | string): Promise<GetCommentsResponse> => {
  try {
    const response = await apiClient.get(`/api/v1/defect/${defectId}/comment`);
    const resData = response.data?.data || response.data;
    const comments = Array.isArray(resData) ? resData : [];

    return {
      status: 'success',
      statusCode: response.status || 200,
      message: 'Comments fetched successfully',
      data: comments.map((c: any) => ({
        id: c.id || c.commentId,
        comment: c.comment || '',
        userId: c.createdBy,
        defectId: Number(defectId),
        attachment: null,
        createdAt: c.createdAt || new Date().toISOString(),
        createdByName: c.createdByName || '',
      })),
    };
  } catch (error: any) {
    return {
      status: 'error',
      statusCode: error.response?.status || 500,
      message: error.response?.data?.message || 'Failed to fetch comments',
      data: [],
    };
  }
};
