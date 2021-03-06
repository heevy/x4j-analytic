/*
 * Copyright 2008-2013 Exigen Insurance Solutions, Inc. All Rights Reserved.
 *
*/


package com.exigeninsurance.x4j.analytic.xlsx.transform.pdf.components.cell;

import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;

import com.exigeninsurance.x4j.analytic.xlsx.transform.MergedRegion;
import com.exigeninsurance.x4j.analytic.xlsx.transform.pdf.PdfCellNode;
import com.exigeninsurance.x4j.analytic.xlsx.transform.pdf.PdfContext;
import com.exigeninsurance.x4j.analytic.xlsx.transform.pdf.PdfRenderer;
import com.exigeninsurance.x4j.analytic.xlsx.transform.pdf.RenderingContext;
import com.exigeninsurance.x4j.analytic.xlsx.transform.pdf.geometry.Point;
import com.exigeninsurance.x4j.analytic.xlsx.transform.pdf.geometry.Range;
import com.exigeninsurance.x4j.analytic.xlsx.transform.pdf.geometry.Rectangle;


public class MergedCellRenderer extends AbstractCellRenderer {

	public MergedCellRenderer(PdfCellNode node ) {
		super(node);
	}

    @Override
    protected Rectangle calculateDrawingArea(RenderingContext context) {
		PdfContext pdfContext = context.getPdfContext();
		if (node.isMerged(pdfContext)) {
            MergedRegion associatedRegion = node.getMergedRegion(pdfContext);
            float x = pdfContext.getX();
            float y = pdfContext.getY();
            float areaTopY = y + getRowHeight(context);
            float margins = (associatedRegion.getRegionHeigth() - 1) * PdfRenderer.ROW_MARGIN;
            float areaBottomY = areaTopY - node.getParent().getHeigth(context, associatedRegion.getVerticalRange()) - margins;
            float containerRightSide = x + node.getParent().getMergedRegionWidth(context, node, associatedRegion);
            float containerHeight = areaTopY - areaBottomY;
            float containerWidth = containerRightSide - x;

            return new Rectangle(new Point(x, areaBottomY), containerWidth, containerHeight);
        }
        else {
            return super.calculateDrawingArea(context);
        }

    }

    @Override
    protected Rectangle calculateFillArea(RenderingContext context, Rectangle drawingArea) {
        return !node.isMerged(context.getPdfContext()) ? null :
                new Rectangle(new Point(
                        drawingArea.getLowerLeft().getX(),
                        drawingArea.getLowerLeft().getY() - PdfRenderer.ROW_MARGIN / 2),
                        drawingArea.getWidth(), drawingArea.getHeight() + PdfRenderer.ROW_MARGIN);
    }

    @Override
    protected void drawText(RenderingContext context, Object value) throws IOException {
		PdfContext pdfContext = context.getPdfContext();
		if (node.isMerged(pdfContext)) {
            if (value != null) {
                String text = node.formatValue(pdfContext, value);
                float x = textArea.getLowerLeft().getX() + findHorizontalOffset(text) + findStartingDrawingPoint(textArea.getWidth());
                float y = textArea.getLowerLeft().getY() + findVerticalOffset(textArea.getHeight());
				setTextOptions(pdfContext);
				pdfContext.drawText(text, x, y);
            }
			pdfContext.movePointerBy(drawingArea.getWidth(), 0);
        }
    }

	@Override
    protected boolean applyFillAndBorders(RenderingContext context) {
        return node.isMerged(context.getPdfContext());
    }

    @Override
    protected void drawBorders(RenderingContext context) {
		PdfContext pdfContext = context.getPdfContext();
		if (node.isMerged(pdfContext)) {
            XSSFCell otherCell = getRegionBottomRightCell(pdfContext);
            drawBorders(pdfContext, otherCell.getCellStyle());
            drawBorders(pdfContext, node.getCellStyle());
        }
    }

    private XSSFCell getRegionBottomRightCell(PdfContext context) {
        MergedRegion associatedRegion = node.getMergedRegion(context);
        Range horizontalRange = associatedRegion.getHorizontalRange();
        Range verticalRange = associatedRegion.getVerticalRange();
        XSSFRow row = context.getSheet().getRow(verticalRange.getLast());
        return row.getCell(horizontalRange.getLast());
    }
}
