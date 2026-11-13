package com.UNI_FYP.AI_Based_Road_Construction.service;

import com.UNI_FYP.AI_Based_Road_Construction.dto.AnalyticsResponse;
import com.UNI_FYP.AI_Based_Road_Construction.dto.CountByLabelResponse;
import com.UNI_FYP.AI_Based_Road_Construction.dto.DepartmentPerformanceResponse;
import com.UNI_FYP.AI_Based_Road_Construction.dto.MonthlyTrendResponse;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ReportService {

    private final AnalyticsService   analyticsService;
    private final PerformanceService performanceService;

    private static final Font TITLE   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font SECTION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
    private static final Font NORMAL  = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font HEADER  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);

    public byte[] generateSummaryReport() {
        try {
            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 40, 36);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(doc, out);
            doc.open();

            AnalyticsResponse a = analyticsService.getAnalytics();

            doc.add(new Paragraph("Road Work Coordination System", TITLE));
            doc.add(new Paragraph("Summary Report", SECTION));
            doc.add(new Paragraph("Generated on " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")), NORMAL));
            doc.add(new Paragraph(" "));

            // Overview
            doc.add(new Paragraph("Overview", SECTION));
            PdfPTable overview = table(new float[]{3, 1}, "Metric", "Value");
            row(overview, "Total requests", a.getTotalRequests());
            row(overview, "Pending (incl. conflict flagged)", a.getPendingRequests());
            row(overview, "Approved", a.getApprovedRequests());
            row(overview, "In progress", a.getInProgressRequests());
            row(overview, "Completed", a.getCompletedRequests());
            row(overview, "Rejected", a.getRejectedRequests());
            row(overview, "Total conflicts", a.getTotalConflicts());
            row(overview, "Unresolved conflicts", a.getPendingConflicts());
            row(overview, "Total complaints", a.getTotalComplaints());
            row(overview, "Open complaints", a.getOpenComplaints());
            row(overview, "Inspection reports", a.getTotalInspections());
            doc.add(overview);
            doc.add(new Paragraph(" "));

            breakdown(doc, "Requests by status", a.getRequestsByStatus());
            breakdown(doc, "Requests by work type", a.getRequestsByWorkType());
            breakdown(doc, "Complaints by category", a.getComplaintsByCategory());
            breakdown(doc, "Inspections by verdict", a.getInspectionsByVerdict());

            // Department performance
            doc.add(new Paragraph("Department performance", SECTION));
            PdfPTable perf = table(new float[]{3, 1, 1, 1, 1, 1, 1.3f, 1.3f, 1.1f, 1.2f, 1.3f},
                    "Department", "Total", "Pending", "Active", "Completed", "Rejected",
                    "Completion", "Approval", "Avg days", "Inspections", "Restored");
            for (DepartmentPerformanceResponse d : performanceService.getDepartmentPerformance()) {
                cell(perf, d.getDepartmentName());
                cell(perf, String.valueOf(d.getTotalRequests()));
                cell(perf, String.valueOf(d.getPending()));
                cell(perf, String.valueOf(d.getActive()));
                cell(perf, String.valueOf(d.getCompleted()));
                cell(perf, String.valueOf(d.getRejected()));
                cell(perf, pct(d.getCompletionRate()));
                cell(perf, pct(d.getApprovalRate()));
                cell(perf, d.getAvgPlannedDays() == null ? "-" : String.valueOf(d.getAvgPlannedDays()));
                cell(perf, String.valueOf(d.getInspections()));
                cell(perf, pct(d.getRestorationRate()));
            }
            doc.add(perf);
            doc.add(new Paragraph(" "));

            // Monthly trend
            doc.add(new Paragraph("Last 6 months", SECTION));
            PdfPTable trend = table(new float[]{2, 1, 1}, "Month", "Submitted", "Completed");
            for (MonthlyTrendResponse m : performanceService.getMonthlyTrend()) {
                cell(trend, m.getMonth());
                cell(trend, String.valueOf(m.getSubmitted()));
                cell(trend, String.valueOf(m.getCompleted()));
            }
            doc.add(trend);

            doc.close();
            return out.toByteArray();

        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private void breakdown(Document doc, String title, List<CountByLabelResponse> rows)
            throws DocumentException {
        doc.add(new Paragraph(title, SECTION));
        PdfPTable t = table(new float[]{3, 1}, "Label", "Count");
        for (CountByLabelResponse r : rows) {
            cell(t, r.getLabel().replace('_', ' '));
            cell(t, String.valueOf(r.getCount()));
        }
        doc.add(t);
        doc.add(new Paragraph(" "));
    }

    private PdfPTable table(float[] widths, String... headers) throws DocumentException {
        PdfPTable t = new PdfPTable(widths.length);
        t.setWidthPercentage(100);
        t.setWidths(widths);
        t.setSpacingBefore(6f);
        for (String h : headers) {
            PdfPCell c = new PdfPCell(new Phrase(h, HEADER));
            c.setBackgroundColor(Color.LIGHT_GRAY);
            c.setPadding(4f);
            t.addCell(c);
        }
        return t;
    }

    private void row(PdfPTable t, String label, Object value) {
        cell(t, label);
        cell(t, String.valueOf(value));
    }

    private void cell(PdfPTable t, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, NORMAL));
        c.setPadding(4f);
        t.addCell(c);
    }

    private String pct(Double v) {
        return v == null ? "-" : v + "%";
    }
}