import { QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Route, Routes, Navigate } from "react-router-dom";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { Toaster } from "@/components/ui/toaster";
import { TooltipProvider } from "@/components/ui/tooltip";
import { RoleProvider } from "@/contexts/RoleContext";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { queryClient } from "@/lib/query-client";

// Public
import Login from "./pages/Login";
import NotFound from "./pages/NotFound";

// Admin
import AdminDashboard from "./pages/admin/AdminDashboard";
import AdminDoctors from "./pages/admin/AdminDoctors";
import AdminPatients from "./pages/admin/AdminPatients";
import AdminAppointments from "./pages/admin/AdminAppointments";
import AdminDepartments from "./pages/admin/AdminDepartments";
import AdminBuilding from "./pages/admin/AdminBuilding";
import AdminReports from "./pages/admin/AdminReports";
import AdminStaff from "./pages/admin/AdminStaff";
import AdminAccessControl from "./pages/admin/AdminAccessControl";

// Doctor
import DoctorDashboard from "./pages/doctor/DoctorDashboard";
import DoctorAppointments from "./pages/doctor/DoctorAppointments";
import DoctorPatients from "./pages/doctor/DoctorPatients";
import DoctorPatientRecord from "./pages/doctor/DoctorPatientRecord";
import DoctorSchedule from "./pages/doctor/DoctorSchedule";
import DoctorPrescriptions from "./pages/doctor/DoctorPrescriptions";
import DoctorNotes from "./pages/doctor/DoctorNotes";
import DoctorConsultationNote from "./pages/doctor/DoctorConsultationNote";

// Patient
import PatientDashboard from "./pages/patient/PatientDashboard";
import PatientAppointments from "./pages/patient/PatientAppointments";
import BookAppointment from "./pages/patient/BookAppointment";
import PatientRecords from "./pages/patient/PatientRecords";
import PatientPrescriptions from "./pages/patient/PatientPrescriptions";
import PatientBilling from "./pages/patient/PatientBilling";
import PatientHealth from "./pages/patient/PatientHealth";
import PatientNotifications from "./pages/patient/PatientNotifications";

const App = () => (
    <QueryClientProvider client={queryClient}>
      <TooltipProvider>
        <Toaster />
        <Sonner />
        <BrowserRouter>
          <RoleProvider>
            <Routes>
              {/* Public */}
              <Route path="/" element={<Navigate to="/login" replace />} />
              <Route path="/login" element={<Login />} />

              {/* Admin */}
              <Route path="/admin" element={<ProtectedRoute allowedRoles={["ADMIN"]}><AdminDashboard /></ProtectedRoute>} />
              <Route path="/admin/patients" element={<ProtectedRoute allowedRoles={["ADMIN"]}><AdminPatients /></ProtectedRoute>} />
              <Route path="/admin/doctors" element={<ProtectedRoute allowedRoles={["ADMIN"]}><AdminDoctors /></ProtectedRoute>} />
              <Route path="/admin/appointments" element={<ProtectedRoute allowedRoles={["ADMIN"]}><AdminAppointments /></ProtectedRoute>} />
              <Route path="/admin/departments" element={<ProtectedRoute allowedRoles={["ADMIN"]}><AdminDepartments /></ProtectedRoute>} />
              <Route path="/admin/building" element={<AdminBuilding />} />
              <Route path="/admin/building/:dept" element={<AdminBuilding />} />
              <Route path="/admin/reports" element={<ProtectedRoute allowedRoles={["ADMIN"]}><AdminReports /></ProtectedRoute>} />
              {/*<Route path="/admin/staff" element={<ProtectedRoute allowedRoles={["ADMIN"]}><AdminStaff /></ProtectedRoute>} /> */}
              {/*<Route path="/admin/access" element={<ProtectedRoute allowedRoles={["ADMIN"]}><AdminAccessControl /></ProtectedRoute>} /> */}

              {/* Doctor */}
              <Route path="/doctor" element={<ProtectedRoute allowedRoles={["DOCTOR"]}><DoctorDashboard /></ProtectedRoute>} />
              <Route path="/doctor/appointments" element={<ProtectedRoute allowedRoles={["DOCTOR"]}><DoctorAppointments /></ProtectedRoute>} />
              <Route path="/doctor/patients" element={<ProtectedRoute allowedRoles={["DOCTOR"]}><DoctorPatients /></ProtectedRoute>} />
              <Route path="/doctor/patients/:patientProfileId" element={<ProtectedRoute allowedRoles={["DOCTOR"]}><DoctorPatientRecord /></ProtectedRoute>} />
              <Route path="/doctor/schedule" element={<ProtectedRoute allowedRoles={["DOCTOR"]}><DoctorSchedule /></ProtectedRoute>} />
              {/*<Route path="/doctor/prescriptions" element={<ProtectedRoute allowedRoles={["DOCTOR"]}><DoctorPrescriptions /></ProtectedRoute>} /> */}
              <Route path="/doctor/notes" element={<ProtectedRoute allowedRoles={["DOCTOR"]}><DoctorNotes /></ProtectedRoute>} />
              <Route path="/doctor/consultation/:appointmentId/:patientProfileId" element={<ProtectedRoute allowedRoles={["DOCTOR"]}><DoctorConsultationNote /></ProtectedRoute>} />

              {/* Patient */}
              <Route path="/patient" element={<ProtectedRoute allowedRoles={["PATIENT"]}><PatientDashboard /></ProtectedRoute>} />
              <Route path="/patient/appointments" element={<ProtectedRoute allowedRoles={["PATIENT"]}><PatientAppointments /></ProtectedRoute>} />
              <Route path="/patient/book" element={<ProtectedRoute allowedRoles={["PATIENT"]}><BookAppointment /></ProtectedRoute>} />
              <Route path="/patient/records" element={<ProtectedRoute allowedRoles={["PATIENT"]}><PatientRecords /></ProtectedRoute>} />
              <Route path="/patient/prescriptions" element={<ProtectedRoute allowedRoles={["PATIENT"]}><PatientPrescriptions /></ProtectedRoute>} />
              <Route path="/patient/billing" element={<ProtectedRoute allowedRoles={["PATIENT"]}><PatientBilling /></ProtectedRoute>} />
              <Route path="/patient/health" element={<ProtectedRoute allowedRoles={["PATIENT"]}><PatientHealth /></ProtectedRoute>} />
              <Route path="/patient/notifications" element={<ProtectedRoute allowedRoles={["PATIENT"]}><PatientNotifications /></ProtectedRoute>} />

              <Route path="*" element={<NotFound />} />
            </Routes>
          </RoleProvider>
        </BrowserRouter>
      </TooltipProvider>
    </QueryClientProvider>
);

export default App;