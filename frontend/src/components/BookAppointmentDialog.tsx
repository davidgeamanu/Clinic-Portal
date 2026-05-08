import { useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { Calendar } from "@/components/ui/calendar";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { CalendarIcon, Video, MapPin } from "lucide-react";
import { cn } from "@/lib/utils";
import { format } from "date-fns";
import { useToast } from "@/hooks/use-toast";

const departments = ["Cardiology", "Neurology", "General Medicine", "Pediatrics", "Orthopedics", "Dermatology"];

const doctorsByDept: Record<string, string[]> = {
  Cardiology: ["Dr. James Williams", "Dr. Robert Taylor"],
  Neurology: ["Dr. Sarah Chen"],
  "General Medicine": ["Dr. Lisa Park", "Dr. Anna White"],
  Pediatrics: ["Dr. Kim Lee"],
  Orthopedics: ["Dr. David Brown"],
  Dermatology: ["Dr. Emily Stone"],
};

const timeSlots = [
  "09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM",
  "01:00 PM", "01:30 PM", "02:00 PM", "02:30 PM", "03:00 PM", "03:30 PM", "04:00 PM",
];

const appointmentTypes = ["Consultation", "Follow-up", "Check-up", "Emergency", "Annual Physical"];

interface BookAppointmentDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /** If provided, pre-fills the patient name field and hides it (for patient portal) */
  patientName?: string;
  /** If provided, pre-fills patient field as editable (for admin) */
  showPatientField?: boolean;
}

export function BookAppointmentDialog({ open, onOpenChange, patientName, showPatientField = false }: BookAppointmentDialogProps) {
  const [date, setDate] = useState<Date>();
  const [department, setDepartment] = useState("");
  const [doctor, setDoctor] = useState("");
  const [time, setTime] = useState("");
  const [type, setType] = useState("");
  const [mode, setMode] = useState("In-Person");
  const [notes, setNotes] = useState("");
  const [patient, setPatient] = useState("");
  const { toast } = useToast();

  const availableDoctors = department ? doctorsByDept[department] ?? [] : [];

  const handleSubmit = () => {
    if (!date || !department || !doctor || !time || !type) {
      toast({ title: "Missing fields", description: "Please fill in all required fields.", variant: "destructive" });
      return;
    }
    toast({ title: "Appointment booked!", description: `${format(date, "PPP")} at ${time} with ${doctor}` });
    // Reset
    setDate(undefined);
    setDepartment("");
    setDoctor("");
    setTime("");
    setType("");
    setMode("In-Person");
    setNotes("");
    setPatient("");
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[520px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Book New Appointment</DialogTitle>
        </DialogHeader>
        <div className="grid gap-4 py-2">
          {/* Patient field (admin only) */}
          {showPatientField && (
            <div className="space-y-2">
              <Label>Patient Name *</Label>
              <Input placeholder="Enter patient name..." value={patient} onChange={(e) => setPatient(e.target.value)} />
            </div>
          )}

          {/* Department */}
          <div className="space-y-2">
            <Label>Department *</Label>
            <Select value={department} onValueChange={(v) => { setDepartment(v); setDoctor(""); }}>
              <SelectTrigger><SelectValue placeholder="Select department" /></SelectTrigger>
              <SelectContent>
                {departments.map((d) => <SelectItem key={d} value={d}>{d}</SelectItem>)}
              </SelectContent>
            </Select>
          </div>

          {/* Doctor */}
          <div className="space-y-2">
            <Label>Doctor *</Label>
            <Select value={doctor} onValueChange={setDoctor} disabled={!department}>
              <SelectTrigger><SelectValue placeholder={department ? "Select doctor" : "Select department first"} /></SelectTrigger>
              <SelectContent>
                {availableDoctors.map((d) => <SelectItem key={d} value={d}>{d}</SelectItem>)}
              </SelectContent>
            </Select>
          </div>

          {/* Date picker */}
          <div className="space-y-2">
            <Label>Date *</Label>
            <Popover>
              <PopoverTrigger asChild>
                <Button variant="outline" className={cn("w-full justify-start text-left font-normal", !date && "text-muted-foreground")}>
                  <CalendarIcon className="mr-2 h-4 w-4" />
                  {date ? format(date, "PPP") : "Pick a date"}
                </Button>
              </PopoverTrigger>
              <PopoverContent className="w-auto p-0" align="start">
                <Calendar
                  mode="single"
                  selected={date}
                  onSelect={setDate}
                  disabled={(d) => d < new Date()}
                  initialFocus
                  className={cn("p-3 pointer-events-auto")}
                />
              </PopoverContent>
            </Popover>
          </div>

          {/* Time */}
          <div className="space-y-2">
            <Label>Time Slot *</Label>
            <Select value={time} onValueChange={setTime}>
              <SelectTrigger><SelectValue placeholder="Select time" /></SelectTrigger>
              <SelectContent>
                {timeSlots.map((t) => <SelectItem key={t} value={t}>{t}</SelectItem>)}
              </SelectContent>
            </Select>
          </div>

          {/* Type */}
          <div className="space-y-2">
            <Label>Appointment Type *</Label>
            <Select value={type} onValueChange={setType}>
              <SelectTrigger><SelectValue placeholder="Select type" /></SelectTrigger>
              <SelectContent>
                {appointmentTypes.map((t) => <SelectItem key={t} value={t}>{t}</SelectItem>)}
              </SelectContent>
            </Select>
          </div>

          {/* Mode toggle */}
          <div className="space-y-2">
            <Label>Mode</Label>
            <div className="flex gap-2">
              <Button
                type="button"
                variant={mode === "In-Person" ? "default" : "outline"}
                className="flex-1 gap-2"
                onClick={() => setMode("In-Person")}
              >
                <MapPin className="h-4 w-4" /> In-Person
              </Button>
              <Button
                type="button"
                variant={mode === "Video" ? "default" : "outline"}
                className="flex-1 gap-2"
                onClick={() => setMode("Video")}
              >
                <Video className="h-4 w-4" /> Video Call
              </Button>
            </div>
          </div>

          {/* Notes */}
          <div className="space-y-2">
            <Label>Notes (optional)</Label>
            <Textarea placeholder="Describe your symptoms or reason for visit..." value={notes} onChange={(e) => setNotes(e.target.value)} rows={3} />
          </div>

          {/* Actions */}
          <div className="flex gap-2 pt-2">
            <Button variant="outline" className="flex-1" onClick={() => onOpenChange(false)}>Cancel</Button>
            <Button className="flex-1" onClick={handleSubmit}>Confirm Booking</Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
