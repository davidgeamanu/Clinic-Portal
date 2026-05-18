import { Document, Page, Text, View, StyleSheet, Font } from "@react-pdf/renderer";
import type { DoctorNoteListItem } from "@/types/api";

const styles = StyleSheet.create({
  page: {
    padding: "40 50",
    fontFamily: "Helvetica",
    fontSize: 10,
    color: "#334155",
    backgroundColor: "#ffffff",
  },

  // Brand Header
  brandHeader: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    borderBottomWidth: 1.5,
    borderBottomColor: "#0f172a",
    paddingBottom: 20,
    marginBottom: 25,
  },
  brandName: {
    fontSize: 22,
    fontFamily: "Helvetica-Bold",
    color: "#0f172a",
    letterSpacing: -0.5,
  },
  brandTagline: {
    fontSize: 9,
    textTransform: "uppercase",
    color: "#64748b",
    marginTop: 2,
    letterSpacing: 1,
  },
  headerInfo: {
    textAlign: "right",
  },
  appointmentId: {
    fontSize: 10,
    fontFamily: "Helvetica-Bold",
    color: "#0f172a",
  },
  dateText: {
    fontSize: 9,
    color: "#64748b",
    marginTop: 4,
  },

  // Patient Info Grid
  infoGrid: {
    flexDirection: "row",
    backgroundColor: "#f8fafc",
    borderRadius: 6,
    padding: 15,
    marginBottom: 30,
    borderWidth: 0.5,
    borderColor: "#e2e8f0",
  },
  infoColumn: {
    flex: 1,
  },
  infoLabel: {
    fontSize: 7,
    fontFamily: "Helvetica-Bold",
    color: "#94a3b8",
    textTransform: "uppercase",
    marginBottom: 4,
  },
  infoValue: {
    fontSize: 10,
    color: "#1e293b",
    fontFamily: "Helvetica-Bold",
  },

  // Section Styling
  section: {
    marginBottom: 22,
  },
  sectionTitleWrapper: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 8,
  },
  sectionTitle: {
    fontSize: 11,
    fontFamily: "Helvetica-Bold",
    color: "#0f172a",
    width: 120, // Fixed width for a "label" look
  },
  sectionContent: {
    fontSize: 10,
    lineHeight: 1.6,
    color: "#334155",
    paddingLeft: 10,
    borderLeftWidth: 1,
    borderLeftColor: "#e2e8f0",
  },

  // Signature Block
  signatureSection: {
    marginTop: 40,
    flexDirection: "row",
    justifyContent: "flex-end",
  },
  signatureLine: {
    width: 180,
    borderTopWidth: 1,
    borderTopColor: "#cbd5e1",
    paddingTop: 8,
    textAlign: "center",
  },
  signatureText: {
    fontSize: 9,
    fontFamily: "Helvetica-Bold",
    color: "#1e293b",
  },
  signatureSub: {
    fontSize: 8,
    color: "#94a3b8",
    marginTop: 2,
  },

  // Footer
  footer: {
    position: "absolute",
    bottom: 30,
    left: 50,
    right: 50,
    borderTopWidth: 0.5,
    borderTopColor: "#e2e8f0",
    paddingTop: 10,
    flexDirection: "row",
    justifyContent: "space-between",
  },
  footerText: {
    fontSize: 8,
    color: "#94a3b8",
    fontStyle: "italic",
  },
});

function SectionBlock({ title, content }: { title: string; content: string | null }) {
  if (!content) return null;
  return (
      <View style={styles.section}>
        <View style={styles.sectionTitleWrapper}>
          <Text style={styles.sectionTitle}>{title}</Text>
        </View>
        <View style={styles.sectionContent}>
          <Text>{content}</Text>
        </View>
      </View>
  );
}

function formatDate(dateStr: string) {
  return new Date(dateStr + "T00:00:00").toLocaleDateString("en-GB", {
    day: "2-digit", month: "long", year: "numeric",
  });
}

export function ConsultationReportPDF({
                                        note,
                                        doctorName,
                                        doctorSpecialization,
                                      }: {
  note: DoctorNoteListItem;
  doctorName: string;
  doctorSpecialization: string;
}) {
  const generated = new Date().toLocaleDateString("en-GB", {
    day: "2-digit", month: "long", year: "numeric",
  });

  return (
      <Document
          title={`Consultation_Report_${note.patientName.replace(/\s/g, '_')}`}
          author={`Dr. ${doctorName}`}
      >
        <Page size="A4" style={styles.page}>
          {/* Header */}
          <View style={styles.brandHeader}>
            <View>
              <Text style={styles.brandName}>Clinic Portal</Text>
              <Text style={styles.brandTagline}>Official Medical Record</Text>
            </View>
            <View style={styles.headerInfo}>
              <Text style={styles.appointmentId}>ID: REF-{note.appointmentId}</Text>
              <Text style={styles.dateText}>{formatDate(note.appointmentDate)}</Text>
            </View>
          </View>

          {/* Info Grid */}
          <View style={styles.infoGrid}>
            <View style={styles.infoColumn}>
              <Text style={styles.infoLabel}>Patient Name</Text>
              <Text style={styles.infoValue}>{note.patientName}</Text>
            </View>
            <View style={styles.infoColumn}>
              <Text style={styles.infoLabel}>Attending Physician</Text>
              <Text style={styles.infoValue}>Dr. {doctorName}</Text>
            </View>
            <View style={styles.infoColumn}>
              <Text style={styles.infoLabel}>Department</Text>
              <Text style={styles.infoValue}>{doctorSpecialization || "General Medicine"}</Text>
            </View>
          </View>

          {/* Clinical Content */}
          <SectionBlock title="Diagnosis" content={note.diagnosis} />
          <SectionBlock title="Treatment Plan" content={note.treatment} />
          <SectionBlock title="Prescription" content={note.prescription} />
          <SectionBlock title="Clinical Notes" content={note.notes} />

          {/* Signature Area */}
          <View style={styles.signatureSection}>
            <View style={styles.signatureLine}>
              <Text style={styles.signatureText}>Dr. {doctorName}</Text>
              <Text style={styles.signatureSub}>Electronically Signed</Text>
            </View>
          </View>

          {/* Footer */}
          <View style={styles.footer} fixed>
            <Text style={styles.footerText}>Confidential Document - For Medical Use Only</Text>
            <Text style={styles.footerText}>Printed on: {generated}</Text>
          </View>
        </Page>
      </Document>
  );
}