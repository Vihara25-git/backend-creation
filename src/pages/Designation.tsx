import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Modal } from '../components/ui/Modal';
import { ChevronLeft, Plus, Edit2, Trash2, Briefcase } from 'lucide-react';
import { Toast } from '../components/ui/Toast';
import { createDesignation, Designations, getDesignations,deleteDesignation,putDesignation } from '../api/designation/designation';
import { usePermission } from '../context/PermissionContext';
import { OrbitProgress } from 'react-loading-indicators';

const Designation: React.FC = () => {
  const navigate = useNavigate();
  const [designations, setDesignations] = useState<Designations[]>([]);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingDesignation, setEditingDesignation] = useState<Designations | null>(null);
  const [deletingDesignation, setDeletingDesignation] = useState<Designations | null>(null);
  const [formData, setFormData] = useState({ name: '' });
  const [nextId, setNextId] = useState(1);
  const [totalPages, setTotalPages] = useState(0)

   const { can, allPermissions, hasPermission } = usePermission();

  const [toast, setToast] = useState<{
    isOpen: boolean;
    message: string;
    type: 'success' | 'error';
  }>({ isOpen: false, message: '', type: 'success' });

  
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 5;
  
  const showToast = (message: string, type: 'success' | 'error' = 'success') => {
    setToast({ isOpen: true, message, type });
  };

  const fetchDesignations = async (page :number, size:number) => {
    try {
      setLoading(true)
      const data = await getDesignations(page,size);
      console.log('data.data:', data.data); 
      setTotalPages(data.data.totalPages)
      setDesignations(data.data.content); 
    } catch (error) {
      showToast('Failed to load designations.', 'error');
      setLoading(false)
    } finally {
      setLoading(false)
    }
  };

  const [loading, setLoading] = useState(false);

 useEffect(() => {
  

  fetchDesignations(currentPage-1,pageSize);
  setToast({ isOpen: false, message: '', type: 'success' });
}, [currentPage]);

  const resetForm = () => {
    setFormData({ name: '' });
    setToast({ isOpen: false, message: '', type: 'success' });
  };
  

  const handleCreate = async () => {
    const trimmed = formData.name.trim();

    if (!trimmed) {
      showToast("Designation name cannot be empty", "error");
      return;
    }

    try {
      const response = await createDesignation({ name: trimmed });

      await fetchDesignations(currentPage - 1, pageSize);

      setIsCreateModalOpen(false);
      resetForm();

      showToast(response?.statusMessage || response?.message || "Designation created successfully!", "success");

    } catch(error: any){
      const errorMsg = error?.response?.data?.statusMessage || error?.response?.data?.message || "Failed to create designation";
      showToast(errorMsg, "error");
    }
  };

  const handleEdit = async () => {
    if (!editingDesignation) return;
    const trimmed = formData.name.trim();
    if (trimmed === editingDesignation.name.trim()) {
      showToast('No changes were made to the designation', 'error');
      return;
    }
    const exists = designations.some(
      d => d.name.trim().toLowerCase() === trimmed.toLowerCase() && d.id !== editingDesignation.id
    );
    if (exists) {
      setIsEditModalOpen(false);
      setEditingDesignation(null);
      resetForm();
      showToast('Designation already exists.', 'error');
      return;
    }
    try {
      const response = await putDesignation(editingDesignation.id, { name: trimmed }); 
      setDesignations(prev => prev.map(d => d.id === editingDesignation.id ? { ...d, name: trimmed } : d));
      setIsEditModalOpen(false);
      setEditingDesignation(null);
      resetForm();
      showToast(response?.statusMessage || response?.message || "Designation updated successfully!", "success");
    } catch (error: any) {
      const errorMsg = error?.response?.data?.statusMessage || error?.response?.data?.message || "Failed to update designation";
      showToast(errorMsg, "error");
    }
  };

  const handleDelete = async () => {
    if (!deletingDesignation) return;
    try {
      const response = await deleteDesignation(deletingDesignation.id); 
      fetchDesignations(0, pageSize);
      setIsDeleteModalOpen(false);
      setDeletingDesignation(null);
      showToast(response?.statusMessage || response?.message || "Designation deleted successfully!", "success");
    } catch (error) {
      showToast('Cannot delete designation. It is currently assigned to employees. Please reassign or delete those employees first.', 'error');
    }
  };

  const openEditModal = (designation: Designations) => {
    setEditingDesignation(designation);
    setFormData({ name: designation.name });
    setIsEditModalOpen(true);
    setToast({ isOpen: false, message: '', type: 'success' });
  };

  const openDeleteModal = (designation: Designations) => {
    setDeletingDesignation(designation);
    setIsDeleteModalOpen(true);
  };

  return (
    <div className="max-w-6xl mx-auto p-8">
      {}
      <Toast
        isOpen={toast.isOpen}
        message={toast.message}
        type={toast.type}
        onClose={() => setToast({ ...toast, isOpen: false })}
      />
      {}
      <div className="mb-6 flex justify-end">
        <Button
          variant="secondary"
          onClick={() => navigate('/configurations')}
          className="flex items-center"
        >
          <ChevronLeft className="w-5 h-5 mr-2" /> Back
        </Button>
      </div>

      {}
      <div className="flex justify-between items-center mb-8">
        <div className="flex items-center">
          <Briefcase className="w-8 h-8 text-blue-500 mr-3" />
          <h1 className="text-3xl font-bold text-gray-900">Designation Management</h1>
        </div>
        {can.designation.create && <Button
          onClick={() => {
            setIsCreateModalOpen(true);
            setToast({ isOpen: false, message: '', type: 'success' });
          }}
          className="flex items-center"
        >
          <Plus className="w-5 h-5 mr-2" />
          Create Designation
        </Button>}
      </div>

      <div className="overflow-x-auto rounded-lg shadow mb-8 max-w-2xl mx-auto">
        {loading ? (
            <div className="flex justify-center items-center py-20 min-h-[200px]">
              <OrbitProgress
                  variant="dotted"
                  color="#3B82F6"
                  size="medium"
                  text="Loading ..."
                  textColor="#6b7280"
              />
            </div>
        ) : (
            <>
              <table className="min-w-full divide-y divide-gray-200 text-base">
                <thead className="bg-gray-50">
                <tr>
                  <th className="px-5 py-3 text-left text-sm font-semibold text-gray-600 uppercase tracking-wider whitespace-nowrap">
                    Designation Name
                  </th>
                  {(can.designation.update || can.designation.delete) && (
                      <th className="px-5 py-3 text-center text-sm font-semibold text-gray-600 uppercase tracking-wider whitespace-nowrap">
                        Actions
                      </th>
                  )}
                </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                {Array.isArray(designations) && designations.length === 0 ? (
                    <tr>
                      <td colSpan={2} className="px-5 py-3 text-center text-gray-500">
                        No designations found.
                      </td>
                    </tr>
                ) : (
                    Array.isArray(designations) &&
                    designations.map((designation) => (
                        <tr key={designation.id}>
                          <td className="px-5 py-3 whitespace-nowrap font-semibold text-gray-900 text-base">
                            {designation.name}
                          </td>
                          <td className="px-5 py-3 whitespace-nowrap text-center">
                            {can.designation.edit && (
                                <button
                                    onClick={() => openEditModal(designation)}
                                    className="p-1 text-blue-600 hover:text-blue-800 hover:bg-blue-50 rounded mr-2"
                                    title="Edit"
                                >
                                  <Edit2 className="w-5 h-5" />
                                </button>
                            )}
                            {can.designation.delete && (
                                <button
                                    onClick={() => openDeleteModal(designation)}
                                    className="p-1 text-red-600 hover:text-red-800 hover:bg-red-50 rounded"
                                    title="Delete"
                                >
                                  <Trash2 className="w-5 h-5" />
                                </button>
                            )}
                          </td>
                        </tr>
                    ))
                )}
                </tbody>
              </table>

              {}
              {totalPages > 1 && (
                  <div className="flex justify-center items-center gap-2 py-4">
                    <button
                        className="px-3 py-1 rounded border bg-gray-100 text-gray-700 disabled:opacity-50"
                        onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                        disabled={currentPage === 1}
                    >
                      Previous
                    </button>
                    {Array.from({ length: totalPages }, (_, i) => (
                        <button
                            key={i + 1}
                            className={`px-3 py-1 rounded border ${
                                currentPage === i + 1
                                    ? "bg-blue-500 text-white"
                                    : "bg-gray-100 text-gray-700"
                            }`}
                            onClick={() => setCurrentPage(i + 1)}
                        >
                          {i + 1}
                        </button>
                    ))}
                    <button
                        className="px-3 py-1 rounded border bg-gray-100 text-gray-700 disabled:opacity-50"
                        onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
                        disabled={currentPage === totalPages}
                    >
                      Next
                    </button>
                  </div>
              )}
            </>
        )}
      </div>

      {}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => { setIsCreateModalOpen(false); resetForm(); }}
        title="Create New Designation"
      >
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Designation Name
            </label>
            <Input
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder="Enter designation name"
            />
          </div>
          <div className="flex justify-end space-x-3 pt-4">
            <Button
              variant="secondary"
              onClick={() => { setIsCreateModalOpen(false); resetForm(); }}
            >
              Cancel
            </Button>
            <Button onClick={handleCreate} disabled={!formData.name}>
              Create
            </Button>
          </div>
        </div>
      </Modal>

      {}
      <Modal
        isOpen={isEditModalOpen}
        onClose={() => { setIsEditModalOpen(false); setEditingDesignation(null); resetForm(); }}
        title="Edit Designation"
      >
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Designation Name
            </label>
            <Input
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder="Enter designation name"
            />
          </div>
          <div className="flex justify-end space-x-3 pt-4">
            <Button
              variant="secondary"
              onClick={() => { setIsEditModalOpen(false); setEditingDesignation(null); resetForm(); }}
            >
              Cancel
            </Button>
            <Button onClick={handleEdit} disabled={!formData.name}>
              Update
            </Button>
          </div>
        </div>
      </Modal>

      {}
      <Modal
        isOpen={isDeleteModalOpen}
        onClose={() => { setIsDeleteModalOpen(false); setDeletingDesignation(null); }}
        title="Delete Designation"
      >
        <div className="space-y-4">
          <p className="text-gray-700">
            Are you sure you want to delete the designation "{deletingDesignation?.name}"?
            This action cannot be undone.
          </p>
          <div className="flex justify-end space-x-3 pt-4">
            <Button
              variant="secondary"
              onClick={() => { setIsDeleteModalOpen(false); setDeletingDesignation(null); }}
            >
              Cancel
            </Button>
            <Button variant="danger" onClick={handleDelete}>
              Delete
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default Designation;