

import React, { useState, useEffect } from "react";
import { Button } from "../components/ui/Button";
import { Modal } from "../components/ui/Modal";
import { Input } from "../components/ui/Input";
import { Server, Plus, Edit, Trash2, Mail, CheckCircle, AlertCircle, Eye, EyeOff } from "lucide-react";
import { SmtpConfig, ToastState } from "../types/emailConfiguration";
import { getSmtpConfigs, createSmtpConfig, updateSmtpConfig, deleteSmtpConfig, CreateSmtpConfigRequest, updateSmtpConfigStatus } from "../services/emailConfigurationApi";
import { usePermission } from "../context/PermissionContext";

export const SmtpServerTab: React.FC = () => {
  const [configs, setConfigs] = useState<SmtpConfig[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingConfig, setEditingConfig] = useState<SmtpConfig | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<SmtpConfig | null>(null);
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [formData, setFormData] = useState<CreateSmtpConfigRequest>({
    name: '', smtpHost: '', smtpPort: 587, username: '', password: '', fromEmail: '', fromName: '',isEnabled: false
  });
  const [toast, setToast] = useState<ToastState>({
    show: false, message: '', type: 'success'
  });

  const [savingStates, setSavingStates] = useState<Record<number, boolean>>({});

  const { can } = usePermission();

  useEffect(() => {
    loadConfigs();
  }, []);

  const sortConfigs = (list: SmtpConfig[]): SmtpConfig[] => {
    return [...list].sort((a, b) => {
      const aActive = a.isEnabled ? 1 : 0;
      const bActive = b.isEnabled ? 1 : 0;
      if (bActive !== aActive) {
        return bActive - aActive;
      }
      return (a.id ?? 0) - (b.id ?? 0);
    });
  };

  const loadConfigs = async () => {
    try {
      const data = await getSmtpConfigs();

      const mappedData = data.map((item: any) => ({
        ...item,
        isEnabled: item.isEnabled ?? item.is_enabled ?? false,
      }));

      setConfigs(sortConfigs(mappedData));
    } catch (error) {
      console.error("Error loading SMTP configs:", error);
      setConfigs([]);
    }
  };
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      if (editingConfig) {
        await updateSmtpConfig(editingConfig.id, formData);
        showToast('Configuration updated successfully', 'success');
      } else {
        await createSmtpConfig(formData);
        showToast('Configuration created successfully', 'success');
      }
      await loadConfigs();
      resetForm();
      setModalOpen(false);
    } catch (error: any) {
      console.error('Error saving SMTP config:', error);
      const backendMessage =
        error?.response?.data?.data?.[0]?.message ||
        error?.response?.data?.statusMessage ||
        error?.response?.data?.message ||
        error?.response?.data?.error ||
        error?.message ||
        'Failed to save configuration';
      showToast(backendMessage, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;

    if (deleteTarget.isEnabled) {
      showToast(
        "An enabled SMTP Server cannot be deleted. Please disable it first.",
        "error"
      );
      setDeleteTarget(null);
      return;
    }

    setLoading(true);

    try {
      const response = await deleteSmtpConfig(deleteTarget.id);

      await loadConfigs();
      setDeleteTarget(null);

      showToast(
        response?.message || 'Configuration deleted successfully',
        'success'
      );

    } catch (error: any) {
      console.error('Error deleting SMTP config:', error);

      const backendMessage =
        error?.response?.data?.data?.[0]?.message ||
        error?.response?.data?.statusMessage ||
        error?.response?.data?.message ||
        error?.response?.data?.error ||
        error?.message ||
        'Failed to delete configuration';
      setDeleteTarget(null);
      showToast(backendMessage, 'error');

    } finally {
      setLoading(false);
    }
  };

  const handleToggleStatus = async (config: SmtpConfig) => {
    const newStatus = !config.isEnabled;

    setSavingStates(prev => ({ ...prev, [config.id]: true }));

    // Optimistically update list: only one can be enabled at a time, and it moves to the top
    setConfigs(prev => {
      const updated = prev.map(item => {
        if (item.id === config.id) {
          return { ...item, isEnabled: newStatus };
        }
        if (newStatus) {
          return { ...item, isEnabled: false };
        }
        return item;
      });
      return sortConfigs(updated);
    });

    try {
      await updateSmtpConfigStatus(config.id, newStatus);
      await loadConfigs();

      showToast(
        `SMTP configuration ${newStatus ? "enabled" : "disabled"} successfully`,
        "success"
      );
    } catch (error: any) {
      await loadConfigs();
      const backendMessage =
        error?.response?.data?.data?.[0]?.message ||
        error?.response?.data?.statusMessage ||
        error?.response?.data?.message ||
        'Failed to update SMTP status';
      showToast(backendMessage, "error");
    } finally {
      setSavingStates(prev => ({ ...prev, [config.id]: false }));
    }
  };

  const handleEdit = (config: SmtpConfig) => {
    setEditingConfig(config);
    setFormData({
      name: config.name,
      smtpHost: config.smtpHost,
      smtpPort: config.smtpPort,
      username: config.username,
      password: '',
      fromEmail: config.fromEmail,
      fromName: config.fromName || '',
       isEnabled: Boolean(config.isEnabled),
    });
    setModalOpen(true);
  };

  const resetForm = () => {
    setEditingConfig(null);
    setFormData({ name: '', smtpHost: '', smtpPort: 587, username: '', password: '', fromEmail: '', fromName: '', isEnabled: false });
  };

  const showToast = (message: string, type: 'success' | 'error') => {
    setToast({ show: true, message, type });
    setTimeout(() => setToast({ show: false, message: '', type: 'success' }), 3000);
  };

  return (
    <div className="space-y-6">
      {toast.show && (
        <div className={`fixed top-20 right-4 z-50 flex items-center p-4 rounded-lg shadow-lg ${
          toast.type === 'success' ? 'bg-green-50 text-green-800 border border-green-200' : 'bg-red-50 text-red-800 border border-red-200'
        }`}>
          {toast.type === 'success' ? <CheckCircle className="w-5 h-5 mr-2" /> : <AlertCircle className="w-5 h-5 mr-2" />}
          {toast.message}
        </div>
      )}

      <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-3">
        <div>
          <h2 className="text-lg font-semibold text-gray-900 flex items-center">
            <Server className="w-5 h-5 mr-2 text-blue-500" />
            SMTP Configurations
          </h2>
          <p className="text-sm text-gray-500 mt-1">Manage email server configurations</p>
        </div>
        {can.emailConfig.create &&
            <Button onClick={() => { resetForm(); setModalOpen(true); }} className="flex items-center shadow-sm whitespace-nowrap">
          <Plus className="w-4 h-4 mr-2" />
          Add Configuration
        </Button>}
      </div>

     <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        {configs.length === 0 ? (
          <div className="text-center py-12">
            <Mail className="w-12 h-12 text-gray-300 mx-auto mb-3" />
            <p className="text-gray-500">No SMTP configurations found</p>
            {can.emailConfig.create &&
            <Button variant="outline" onClick={() => { resetForm(); setModalOpen(true); }} className="mt-4">
              Create your first configuration
            </Button>}
          </div>
        ) : (

     <div className="overflow-x-auto">
  <div className="min-w-[1100px]">
    {}
    <div className="grid grid-cols-[1.4fr_1.7fr_1.5fr_0.6fr_1fr_0.7fr] items-center px-7 py-4 bg-gray-50 border-b text-xs font-semibold text-gray-500 uppercase">
      <div>Name</div>
      <div>From Email</div>
      <div>SMTP Email</div>
      <div>Port</div>
      <div>Status</div>
      <div className="text-center">Actions</div>
    </div>

    <div className="divide-y divide-gray-200">
      {configs.map(config => (
        <div
          key={config.id}
          className="grid grid-cols-[1.4fr_1.7fr_1.5fr_0.6fr_1fr_0.7fr] items-center px-5 py-6 hover:bg-gray-50 transition-colors"
        >
          {}
          <div className="flex items-center min-w-0">
            <span
              className={`w-2.5 h-2.5 rounded-full mr-4 flex-shrink-0 ${
                config.isEnabled ? "bg-green-500" : "bg-gray-400"
              }`}
            />
            <h3 className="font-semibold text-gray-900 truncate text-base">
              {config.name}
            </h3>
          </div>

          {}
          <div className="flex items-center min-w-0 text-sm text-gray-500">
            <Mail className="w-5 h-5 mr-3 flex-shrink-0 text-gray-500" />
            <span className="truncate">{config.fromEmail}</span>
          </div>

          {/* SMTP Email */}
          <div className="text-sm text-gray-900 truncate">
            {config.username || config.smtpHost}
          </div>

          {}
          <div className="font-semibold text-gray-700 text-base">
            {config.smtpPort}
          </div>

          {}
          <div className="flex items-center gap-3">
            <button
              onClick={() => handleToggleStatus(config)}
              disabled={savingStates[config.id] || !can.emailConfig.statusUpdate}
              className={`relative inline-flex h-6 w-12 rounded-full border-2 border-transparent transition-colors ${
                config.isEnabled ? "bg-green-600" : "bg-gray-300"
              } ${savingStates[config.id] ? "opacity-50 cursor-wait" : "cursor-pointer"}`}
            >
              <span
                className={`pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow transition ${
                  config.isEnabled ? "translate-x-6" : "translate-x-0"
                }`}
              />
            </button>

            <span
              className={`text-sm font-semibold ${
                config.isEnabled ? "text-green-700" : "text-gray-500"
              }`}
            >
              {config.isEnabled ? "Enabled" : "Disabled"}
            </span>
          </div>

          {}
          <div className="flex items-center justify-center gap-5">
            {can.emailConfig.edit && (
              <button onClick={() => handleEdit(config)} className="text-gray-800 hover:text-blue-600">
                <Edit className="w-5 h-5" />
              </button>
            )}

            {can.emailConfig.delete && (
              <button
                onClick={() => {
                  if (config.isEnabled) {
                    showToast(
                      "An enabled SMTP Server cannot be deleted. Please disable it first.",
                      "error"
                    );
                    return;
                  }
                  setDeleteTarget(config);
                }}
                disabled={config.isEnabled}
                title={
                  config.isEnabled
                    ? "An enabled SMTP Server cannot be deleted. Please disable it first."
                    : "Delete Configuration"
                }
                className={
                  config.isEnabled
                    ? "text-gray-300 cursor-not-allowed opacity-50"
                    : "text-red-600 hover:text-red-700 cursor-pointer"
                }
              >
                <Trash2 className="w-5 h-5" />
              </button>
            )}
          </div>
        </div>
      ))}
    </div>
  </div>
</div>)}
      </div>

      {}
      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editingConfig ? "Edit Configuration" : "Add Configuration"}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input label="Name" value={formData.name} onChange={e => setFormData({ ...formData, name: e.target.value })} placeholder="Production SMTP" required />
          <Input label="SMTP Host" value={formData.smtpHost} onChange={e => setFormData({ ...formData, smtpHost: e.target.value })} placeholder="smtp.gmail.com" required />
          <Input label="SMTP Port" type="number" value={String(formData.smtpPort)} onChange={e => setFormData({ ...formData, smtpPort: Number(e.target.value) })} placeholder="587" required />
          <Input label="Username" value={formData.username} onChange={e => setFormData({ ...formData, username: e.target.value })} placeholder="your-email@gmail.com" required />
          <div className="relative">
            <Input 
              label="Password" 
              type={showPassword ? "text" : "password"} 
              value={formData.password} 
              onChange={e => setFormData({ ...formData, password: e.target.value })} 
              placeholder={editingConfig ? "Leave blank to keep unchanged" : "••••••••"}
              required={!editingConfig}
            />
            <button type="button" onClick={() => setShowPassword(!showPassword)} className="absolute right-3 top-9 text-gray-400">
              {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
            </button>
          </div>
          <Input label="From Email" type="email" value={formData.fromEmail} onChange={e => setFormData({ ...formData, fromEmail: e.target.value })} placeholder="noreply@example.com" required />
          <Input label="From Name" value={formData.fromName} onChange={e => setFormData({ ...formData, fromName: e.target.value })} placeholder="Your Company" />
          <div className="flex justify-end space-x-3 pt-4">
            <Button type="button" variant="outline" onClick={() => setModalOpen(false)}>Cancel</Button>
            <Button type="submit" disabled={loading}>{loading ? 'Saving...' : (editingConfig ? 'Update' : 'Create')}</Button>
          </div>
        </form>
      </Modal>

      {}
      <Modal isOpen={!!deleteTarget} onClose={() => setDeleteTarget(null)} title="Delete Configuration">
        {deleteTarget?.isEnabled ? (
          <div className="space-y-4">
            <div className="flex items-center p-3 text-sm text-red-700 bg-red-50 border border-red-200 rounded-lg">
              <AlertCircle className="w-5 h-5 mr-2 flex-shrink-0" />
              <span>An enabled SMTP Server cannot be deleted. Please disable it first.</span>
            </div>
            <div className="flex justify-end">
              <Button variant="outline" onClick={() => setDeleteTarget(null)}>Close</Button>
            </div>
          </div>
        ) : (
          <div>
            <p className="text-gray-600 mb-4">Are you sure you want to delete <span className="font-medium">{deleteTarget?.name}</span>? This action cannot be undone.</p>
            <div className="flex justify-end space-x-3">
              <Button variant="outline" onClick={() => setDeleteTarget(null)}>Cancel</Button>
              <Button onClick={handleDelete} className="bg-red-600 hover:bg-red-700">{loading ? 'Deleting...' : 'Delete'}</Button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};