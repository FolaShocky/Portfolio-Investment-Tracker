package components;

import market.Stock;
import market.StockItem;

import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.time.LocalDate;
import java.util.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

public class GraphPanel extends JPanel {
    private Point mouse;
    private List<List<Item>> datasets;
    private double scale;

    public GraphPanel() {
        super(new BorderLayout());
        datasets = new ArrayList<>();
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                mouse = null;
                repaint();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                mouse = e.getPoint();
                repaint();
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                mouse = e.getPoint();
                repaint();
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (mouse != null) {
            g.setColor(new Color(200,200,200));
            g.drawLine(mouse.x, 0, mouse.x, this.getHeight());
            g.setColor(Color.black);
        }

        List<Item> hovered = itemsNear(mouse);
        for (List<Item> items : datasets) {
            for (int i = 0; i < items.size(); i++) {
                Item cp = items.get(i);
                Color before = g.getColor();
                g.setColor(cp.color());

                if (i < items.size()-1) {
                    Item np = items.get(i + 1);
                    g.drawLine(cp.getX(), cp.getY(), np.getX(), np.getY());
                }

                if (mouse != null) {
                    if (i < items.size() - 1) {
                        Item np = items.get(i + 1);

                        int left = Math.min(cp.getX(), np.getX());
                        int right = Math.max(cp.getX(), np.getX());

                        if (mouse.x >= left && mouse.x <= right) {
                            double xd = np.getX() - cp.getX();
                            double yd = np.getY() - cp.getY();

                            double mxd = mouse.x - cp.getPos().x;
                            int y = cp.getY() + (int) ((mxd / xd) * yd);

                            int size = 4;

                            g.setColor(cp.color().darker());
                            g.fillOval(mouse.x - size / 2, y - size / 2, size, size);
                        }
                    }
                }

                if (hovered != null && hovered.contains(cp)) {
                    g.setColor(cp.color().darker().darker());
                    g.fillOval(cp.getX() - 7, cp.getY() - 7, 14, 14);
                }

                g.setColor(cp.color().darker());
                g.fillOval(cp.getX() - 5, cp.getY() - 5, 10, 10);

                g.setColor(before);
            }
        }

        if (hovered != null) {
            int x = (int) mouse.getX(),
                y = (int) mouse.getY() - 20;
            int offsetY = 0;

            boolean flip = x >= getWidth()/2;
            Rectangle2D font;

            for (Item anItem : hovered) {
                Color before = g.getColor();
                g.setColor(anItem.color());
                for (String line : anItem.getName().split("\n")) {
                    font = g.getFontMetrics().getStringBounds(line, g);
                    g.setColor(anItem.color().darker().darker());
                    if (flip) {
                        g.fillRect(x - 10 -(int) font.getWidth(), y + offsetY - (int) (font.getHeight()*0.75), (int) font.getWidth(), (int) font.getHeight());
                        g.setColor(anItem.color().brighter());
                        g.drawString(line, x - 10 - (int) font.getWidth(), y + offsetY);
                    } else {
                        g.fillRect(x + 10, y + offsetY - (int) (font.getHeight()*0.75), (int) font.getWidth(), (int) font.getHeight());
                        g.setColor(anItem.color().brighter());
                        g.drawString(line, x + 10, y + offsetY);
                    }
                    offsetY += font.getHeight();
                }
                g.setColor(before);
            }
        }
    }

    /**
     * Removes all datasets from the graph
     */
    public void clear() {
        datasets.clear();
        repaint();
    }

    /**
     * Use a dataset for this {@link GraphPanel}
     *
     * @param s {@link Stock} to use as a dataset
     * @return the {@link GraphPanel} for chaining methods
     */
    public GraphPanel withData(Stock s) {
        return addData(s);
    }

    /**
     * Use a dataset for this {@link GraphPanel}
     *
     * @param s {@link Stock} to use as a dataset
     * @return the {@link GraphPanel} for chaining methods
     */
    public GraphPanel addData(Stock s) {
        Random r = new Random();
        Color color = new Color(r.nextFloat(), r.nextFloat(), r.nextFloat());
        List<Item> dataset = s.getMonth().stream().peek(i -> { if(scale < i.getClose()) scale = i.getClose(); })
                .map(p -> new Item(p, color).as(String.format("%s Close: %,.1f",
                s.symbol(), p.getClose()))).collect(Collectors.toList());

        this.datasets.add(dataset);

        sort();
        repaint();

        return this;
    }

    /**
     * Finds all data-points on the graph "near" the specified point.
     *
     * @param point in the panel to search near
     * @return {@link List} of items near the point
     */
    public List<Item> itemsNear(Point point) {
        return itemsNear(point, 10);
    }

    /**
     * Finds all data-points on the graph "near" the specified point.
     *
     * @param point in the panel to search near
     * @param distance maximum distance from the point to include items
     * @return {@link List} of items near the point
     */
    public List<Item> itemsNear(Point point, int distance) {
        if (point == null)
            return null;

        LocalDate date = LocalDate.now().minusDays((long) 31 -(int) (31 * (point.getX() / (double) getWidth())));
        List<Item> items = new ArrayList<>();

        for (List<Item> dataset : datasets) {
            dataset.stream()
                    .filter(p -> date.equals(p.si.getDate()))
                    .forEach(items::add);
        }

        return items;
    }

    /**
     * Sorts all elements in the graph so that they are displayed properly
     */
    public void sort() {
        for (List<Item> dataset : datasets)
            dataset.sort(Comparator.comparing(a -> a.item().getDate()));
        repaint();
    }

    public class Item {
        StockItem si;
        String display;
        Color color;

        Item(StockItem item, Color color) { this.si = item; this.color = color; }

        int getX() { return (int) (getWidth()*0.03) + (int) (31-si.getDate().until(LocalDate.now(), DAYS)) * getWidth() / 31; }

        int getY() { return getHeight() - (int) ((si.getClose() / scale) * getHeight()) + 15; }

        Point getPos() { return new Point(getX(), getY()); }

        StockItem item() { return si; }

        Color color() { return color; }

        Item as(String display) { this.display = display; return this; }

        String getName() { return (display != null) ? display : si.getLabel(); }
    }
}
