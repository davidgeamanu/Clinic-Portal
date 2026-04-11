A default admin account is seeded on first startup:
- Email: `admin@clinic.com`
- Password: `admin123`



## API Documentation

Swagger UI: `http://localhost:8080/api/swagger-ui/index.html`

---

## Postman Testing

### Authentication

#### Register
`POST /auth/register`

![Register201](<Postman Screenshots/Register_Patient_201.png>)

![Register409](<Postman Screenshots/Register_Patient_409.png>)

#### Login & Login Cookie
JWT `access_token` cookie set after successful login.

![Login Cookie](<Postman Screenshots/Login_Cookie_200.png>)

---

### Patient Flow

#### Get My Profile
`GET /patients/me`

![Get Patient Profile](<Postman Screenshots/GET_Profile_200.png>)

#### Update My Profile
`PUT /patients/me`

![Update Patient Profile](<Postman Screenshots/UPDATE_Profile_200.png>)

---

### Doctor Discovery

#### Get All Doctors
`GET /doctors`

![Get All Doctors](<Postman Screenshots/GET_Doctors_200.png>)

#### Get Doctor by ID
`GET /doctors/{profileId}`

![Get Doctor By ID](<Postman Screenshots/GET_Doctors_byID_200.png>)

#### Get Doctors by Specialization
`GET /doctors/specialization/{specializationId}`

![Get Doctors By Specialization](<Postman Screenshots/GET_Doctors_bySpecialization_200.png>)

---

### Appointments

#### Book Appointment
`POST /appointments`

![Book Appointment](<Postman Screenshots/Create_Appointment_201.png>)

#### Get My Appointments (Patient)
`GET /appointments/my/patient`

![Get Patient Appointments](<Postman Screenshots/GET_Appointments_Patient.png>)

#### Get My Appointments (Doctor)
`GET /appointments/my/doctor`

![Get Doctor Appointments](<Postman Screenshots/GET_Appointments_Doctor.png>)

#### Get Appointment by ID
`GET /appointments/{appointmentId}`

![Get Appointment By ID](<Postman Screenshots/GET_Appointment_byID_200.png>)

#### Confirm Appointment
`PATCH /appointments/{appointmentId}/status`

![Confirm Appointment](<Postman Screenshots/appointment-confirm.png>)

#### Complete Appointment
`PATCH /appointments/{appointmentId}/status`

![Complete Appointment](<Postman Screenshots/Confirm_Appointment_byDoctor_200.png>)

#### Cancel Appointment
`PATCH /appointments/{appointmentId}/status`

![Cancel Appointment](<Postman Screenshots/Cancel_Appointment_200.png>)

#### Slot Already Taken (Error Case)
`POST /appointments`

![Slot Taken](<Postman Screenshots/Create_Appointment_Slot_Taken_409.png>)

---

### Consultation Notes

#### Create Consultation Note
`POST /consultation-notes/appointment/{appointmentId}`

![Create Consultation Note](<Postman Screenshots/Create_Consultation_Note_201.png>)

![Create Consultation Note](<Postman Screenshots/Create_Consultation_Note_400.png>)

#### Get Consultation Note
`GET /consultation-notes/appointment/{appointmentId}`

![Get Consultation Note](<Postman Screenshots/Get_Consultation_Note_200.png>)

---

### Notifications

#### Get All Notifications
`GET /notifications`

![Get Notifications](<Postman Screenshots/Get_Notifications_200.png>)

#### Get Unread Notifications
`GET /notifications/unread`

![Get Unread Notifications](<Postman Screenshots/notifications-unread.png>)

#### Count Unread
`GET /notifications/unread/count`

![Count Unread](<Postman Screenshots/Get_Notification_Count_200.png>)

#### Mark as Read
`PATCH /notifications/{notificationId}/read`

![Mark As Read](<Postman Screenshots/notifications-mark-read.png>)

#### Mark All as Read
`PATCH /notifications/read-all`

![Mark All As Read](<Postman Screenshots/Mark_Notification_As_Read_204.png>)

---

### Admin Flow

#### Create Specialization
`POST /admin/specializations`

![Create Specialization](<Postman Screenshots/ADMIN_Create_Specialization_201png.png>)

#### Get All Specializations
`GET /admin/specializations`

![Get Specializations](<Postman Screenshots/GET_Specializations.png>)

#### Delete Specialization
`DELETE /admin/specializations/{id}`

![Delete Specialization](<Postman Screenshots/DELETE_Specialization_byID.png>)

#### Create Doctor Account
`POST /admin/doctors`

![Create Doctor](<Postman Screenshots/ADMIN_Create_Doctor_201.png>)

#### Get All Users
`GET /admin/users`

![Get All Users](<Postman Screenshots/ADMIN_Get_Users_200.png>)

#### Deactivate User
`PATCH /admin/users/{userId}/status?active=false`

![Deactivate User](<Postman Screenshots/Deactivate_User_200.png>)

![Deactivate User](<Postman Screenshots/ADMIN_Deactivate_User_404.png>)

#### Reactivate User
`PATCH /admin/users/{userId}/status?active=true`

![Reactivate User](<Postman Screenshots/Reactivate_User.png>)