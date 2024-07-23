/**
 * * This plugin implements the contrast stretching operator including manual modifying the transforming function
 * and some ways of transforming by N.N .
 * <p>
 * Source: https://github.com/vlccek/imagej_contrast_stretching
 * </p>
 * Edited by:
 * * Jakub Vlk (2024.07) - adding new methods for contrast stretching, rewriting the whole program to be more
 * readable and understandable. (the changes can be shown in the git history)
 * * Manuel Guillermo Forero
 * * Cristian Morera,
 * * Natalia Hernández
 * * Mariano G. Montero C.
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;

import ij.IJ;
import ij.ImagePlus;
import ij.Undo;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.frame.*;
import ij.process.ImageProcessor;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Contrast_stretching extends PlugInFrame implements ActionListener
{
    public static Label labelminmax; // label for showing the position of the mouse
    static int nControlPoints = 4;
    static double lambda = 0;
    static int lamdaMax = 100;

    // GUI components that are accessed for more places
    static CSPlot graph;
    static Choice chTransferenceMethods;
    static JTextField lambdaField;
    private static Object window;

    // image repr
    private static ImagePlus imp;

    // strings for the methods
    public static final String HYPERBOLIC_TANGENT = "Hyperbolic Tangent";
    public static final String SIGMOID_N_N = "Sigmoid N.N.";
    public static final String SIGMOID_TAN = "Sigmoid Tan";
    public static final String SIGMOID_SIN = "Sigmoid Sin";
    public static final String NATURAL_LOGARITHM = "Natural Logarithm";
    public static final String ROOT = "Root";
    public static final String POWER = "Power";

    // const
    private static final Font sans = new Font("SansSerif", Font.BOLD, 12); //Font for GUI
    private static Font monoFont = new Font("Monospaced", Font.PLAIN, 12); //Font for GUI


    // array fpr for saving methods, their names and functions to apply
    private static generalMethodHolder[] methods;


    public Contrast_stretching()
    {
        super("Contrast Stretching Transform");
        setResizable(false);
    }

    /**
     * Repaint the graph with the new function
     * <p>
     * (dont need to check if the function is difrent than the set one, because the repaint is called only when the function is changed, anyway the graph checkin if the function is the same)
     */
    private static void repaintGraph()
    {
        int choice = chTransferenceMethods.getSelectedIndex();

        graph.setFunction(methods[choice], lambda);
    }

    /**
     * Process the image in Img var and saved to the same var :)
     * DON'T CREATE COPY!
     *
     * @param img procesed img
     */
    private static ImagePlus processImg(ImagePlus img)
    {
        byte[] lut = graph.returnLUT();
        byte[] pixels = (byte[]) (img.getProcessor()).getPixels();
        byte[] output = new byte[pixels.length];

        for (int i = 0; i < pixels.length; i++)
            output[i] = (byte) lut[pixels[i] & 0xff];

        System.arraycopy(output, 0, pixels, 0, output.length);
        return img;
    }

    /**
     * Base on setting in graph creates a new image and show it in the new windows
     */
    private static void preview()
    {
        ImagePlus duplicImg = imp.duplicate();

        ImageProcessor ip = duplicImg.getProcessor();

        ImagePlus processed = processImg(duplicImg); // it's fine, the compiler surely optimize it :))))

        ImageCanvas canvas;
        ImagePlus im;
        im = processed;
        canvas = new ImageCanvas(im);
        ImageWindow imgWin = new ImageWindow(im);
        imgWin.setResizable(false);
        imgWin.setSize(canvas.getSize().width + 150, canvas.getSize().height + 150);
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        imgWin.setLayout(gridbag);
        //Panel Canvas
        c.gridx = 0;
        int y = 0;
        c.gridy = y++;
        c.insets = new Insets(10, 10, 0, 10);
        gridbag.setConstraints(canvas, c);
        imgWin.add(canvas);
        //Panel Time
        Panel panelTime = new Panel();
        c.gridy = y++;
        c.insets = new Insets(2, 10, 0, 10);
        gridbag.setConstraints(panelTime, c);
        imgWin.add(panelTime);
        Panel panelHisto = new Panel();
        c.gridy = y++;
        c.insets = new Insets(2, 10, 0, 10);
        gridbag.setConstraints(panelHisto, c);
        imgWin.add(panelHisto);
        imgWin.show();
    }


    /**
     * Main function of the plugin
     *
     * @param arg argument for the plugin
     */
    public void run(String arg)
    {
        imp = WindowManager.getCurrentImage();
        window = this;
        //Check if the image is null
        if (imp == null)
        {
            IJ.showStatus("No image");
            IJ.noImage();
            return;
        }
        //  ip = imp.getProcessor(); //Get the processor to edit the image values
        int type = imp.getType(); //Get the type of the current image


        //Check if the image is of the correct type
        if ((type == ImagePlus.GRAY16) || (type == ImagePlus.GRAY32))
        {
            IJ.error("Contrast Stretching requires an image of type\n \n8-bit grayscale\n8-bit indexed color\nRGB color");
            return;
        }

        // gui setups
        initMethods();

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel graphPanel = graph();

        JPanel centerPanel = new JPanel(new GridLayout(1, 2));

        JPanel middleColumn = middleColumn();

        JPanel rightColumn = rightColumn();

        centerPanel.add(middleColumn);
        centerPanel.add(rightColumn);

        mainPanel.add(graphPanel, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);


        // Přidání hlavního panelu do okna
        add(mainPanel);


        // Nastavení velikosti okna a viditelnosti
        pack();
        setVisible(true);
    }

    /**
     * Initialize the methods for the contrast stretching HERE IS THE PLACE WHERE YOU SHOULD ADD NEW METHODS
     */
    private static void initMethods()
    {
        // add the methods
        methods = new generalMethodHolder[]{new generalMethodHolder(POWER, new transformationFunction()
        {
            @Override
            public double apply(double value, double lamda)
            {
                return Math.pow(value, lamda);
            }
        }, new transformationOfLambda()
        {
            @Override
            public double apply(double value)
            {
                return value;
            }
        }

        ), new generalMethodHolder(ROOT, new transformationFunction()
        {
            @Override
            public double apply(double value, double lamda)
            {
                return Math.pow(value, 1. / lamda);
            }
        }, new transformationOfLambda()
        {
            @Override
            public double apply(double value)
            {
                return value;
            }
        }), new generalMethodHolder(NATURAL_LOGARITHM, new transformationFunction()
        {
            @Override
            public double apply(double value, double lamda)
            {
                return ((Math.log(value + 1.) / Math.log(256.)) * lamda);
            }
        }, new transformationOfLambda()
        {
            @Override
            public double apply(double value)
            {
                // interval 4-16
                return (value / lamdaMax) * 12 + 4;
            }
        }), new generalMethodHolder(SIGMOID_SIN, new transformationFunction()
        {
            @Override
            public double apply(double value, double alpha_pi)
            {
                return ((1 + (1 / Math.sin(alpha_pi / 2) * Math.sin(alpha_pi * (((double) value) - 0.5)))) / 2);
            }
        }, new transformationOfLambda()
        {
            @Override
            public double apply(double value)
            {
                value = value / lamdaMax;
                if (value == 0) value = 0.1;
                return Math.PI * value;
            }
        }), new generalMethodHolder(SIGMOID_TAN, new transformationFunction()
        {
            @Override
            public double apply(double x, double c)
            {
                double numerator = Math.tan(c * (x - 1));
                double denominator = Math.tan(c / 2) * 2;

                return 1 + (numerator / denominator);
            }
        }, new transformationOfLambda()
        {
            @Override
            public double apply(double value)
            {
                value = value / ((double) lamdaMax / 2);
                if (value == 0) value = 0.1;
                return value;
            }
        }), new generalMethodHolder(SIGMOID_N_N, new transformationFunction()
        {
            @Override
            public double apply(double value, double lambda)
            {
                return (1 / (1 + Math.exp(-2 * lambda * Math.log(255) * ((value) - 0.5))));
            }
        }, new transformationOfLambda()
        {
            @Override
            public double apply(double value)
            {
                return ((value / lamdaMax) * 10) + 1;
            }
        }), new generalMethodHolder(HYPERBOLIC_TANGENT, new transformationFunction()
        {
            @Override
            public double apply(double value, double lamda)
            {
                // Calculate the modified tanh function
                return (Math.tanh((6. * lamda) * value - ((3. * lamda))) + 1.0) / 2.;

            }
        }, new transformationOfLambda()
        {
            @Override
            public double apply(double value)
            {
                return ((value / lamdaMax) * 10) + 1;
            }
        })};
    }

    private static JPanel graph()
    {

        JPanel graphPanel = new JPanel(new BorderLayout());

        // Create min label
        Label minLabel = new Label("0", Label.LEFT);
        minLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Create max label
        Label maxLabel = new Label("255", Label.RIGHT);
        maxLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Create max label for Y axis
        Label maxLabelY = new Label("255", Label.LEFT);
        maxLabelY.setFont(new Font("Monospaced", Font.PLAIN, 12));
        labelminmax = new Label("(---,---)", Label.CENTER);

        // Create a panel for the bottom labels (X axis)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(minLabel, BorderLayout.WEST);
        bottomPanel.add(maxLabel, BorderLayout.EAST);
        bottomPanel.add(labelminmax, BorderLayout.SOUTH);

        // Add the graph component
        JPanel graphContainer = new JPanel(new BorderLayout());
        graph = new CSPlot();
        graphContainer.add(graph, BorderLayout.CENTER);

        // Create a panel for the top label (Y axis)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(maxLabelY, BorderLayout.NORTH);

        // Add components to the main graphPanel
        graphPanel.add(bottomPanel, BorderLayout.SOUTH);
        graphPanel.add(graphContainer, BorderLayout.CENTER);
        graphPanel.add(topPanel, BorderLayout.NORTH);


        return graphPanel;
    }

    private static JPanel middleColumn()
    {
        JPanel middleColumn = new JPanel(new GridLayout(5, 1));
        Label maxLabelY = new Label("Control", Label.LEFT);
        maxLabelY.setFont(sans);
        middleColumn.add(maxLabelY);
        Button previewButton = new Button("Preview");
        middleColumn.add(previewButton);

        previewButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                preview();
            }
        });

        // slider for control poin  ts

        // panel for slider
        JPanel sliderPanel = new JPanel();
        Label labelNControlPoints = new Label("Number of control points:", Label.RIGHT);
        sliderPanel.add(labelNControlPoints);


        final int minValue = 3;
        final int maxValue = 20;
        final int initialValue = 3;
        final JSlider slider = new JSlider(JSlider.HORIZONTAL, minValue, maxValue, initialValue);

        sliderPanel.add(slider);
        // text field to showing the number of control points
        final JTextField textField = new JTextField(String.valueOf(initialValue), 5);
        textField.setHorizontalAlignment(JTextField.CENTER);
        sliderPanel.add(textField);

        // listener for updating the text field
        slider.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                final int numPoints = slider.getValue();
                textField.setText(String.valueOf(numPoints));

                if (minValue >= 3 && numPoints <= maxValue)
                {
                    nControlPoints = numPoints;
                }
                graph.setNControlPoints(nControlPoints);
            }
        });

        middleColumn.add(sliderPanel);


        final Checkbox chp = new Checkbox("Use interpolation between points", true);

        middleColumn.add(chp);

        chp.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                boolean state = chp.getState();
                graph.setRegime(!chp.getState() ? CSPlot.regimeOfPlotting.ACCURATE : CSPlot.regimeOfPlotting.CONTROL_POINTS);
            }
        });
        Button appButton = new Button("Apply");
        middleColumn.add(appButton);

        appButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ImagePlus processed = processImg(imp);
                imp.updateAndRepaintWindow();
                imp.changes = true;
                Undo.setup(Undo.COMPOUND_FILTER_DONE, imp);
                ((Frame) window).dispose();
            }
        });

        return middleColumn;
    }

    private static JPanel rightColumn()
    {
        JPanel rightColumn = new JPanel(new GridLayout(4, 1));
        Label maxLabelY = new Label("Function settings:", Label.LEFT);
        maxLabelY.setFont(sans);
        rightColumn.add(maxLabelY);

        chTransferenceMethods = new Choice();

        for (generalMethodHolder method : methods)
        {
            chTransferenceMethods.add(method.getName());
        }

        // listening on change of chose item
        chTransferenceMethods.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                repaintGraph();
                System.out.println("Method: " + chTransferenceMethods.getSelectedItem());
            }
        });

        rightColumn.add(chTransferenceMethods);

        // lambda param
        final Panel panelInputl = new Panel();
        Label lambdaLabel = new Label("Lambda:");
        lambdaLabel.setFont(sans);
        panelInputl.add(lambdaLabel);
        lambdaField = new JTextField(4);
        lambdaField.setToolTipText("Put a number for try a Tranference Method");
        lambdaField.setText("0");
        panelInputl.add(lambdaField);
        lambdaField.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                lamdaParsing();
            }
        });

//        // shift (on one cares about duplicate :))
//        Label shiftLabel = new Label("Shift:");
//        shiftLabel.setFont(sans);
//        panelInputl.add(shiftLabel);
//        final JTextField shiftField = new JTextField(4);
//        shiftField.setToolTipText("Put a number for try a Transference Method");
//        shiftField.setText("0");
//        panelInputl.add(shiftField);
//        shiftField.addActionListener(new ActionListener()
//        {
//            public void actionPerformed(ActionEvent e)
//            {
//                shift = Double.parseDouble(shiftField.getText());
//            }
//        });
//
//        panelInputl.add(shiftField);
        Button btnApplayMethod = new Button("Apply values to graph");

        btnApplayMethod.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                lamdaParsing();
            }
        });
        panelInputl.add(btnApplayMethod);

        rightColumn.add(panelInputl);

        return rightColumn;
    }

    public Contrast_stretching(String title)
    {
        super(title);
    }


    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
        // let's pray that the compiler will remove this function from asm... PLEASE JAVA BE NICE TO ME
        // no-ťodo :)
    }


    private static void lamdaParsing()
    {
        // in case that the param is not in the right range create popup

        lambda = Double.parseDouble(lambdaField.getText());

        if (lambda < 0 || lambda > 100)
        {
            JOptionPane.showMessageDialog(null, "Lambda must be in the range <0-100>", "Error", JOptionPane.ERROR_MESSAGE);
            lambda = 0;
        }

        // after parsing the lambda, repaint the graph
        repaintGraph();
    }
}


class CSPlot extends Canvas implements MouseMotionListener, MouseListener
{
    private static final int WIDTH = 255, HEIGHT = 255;
    private int nControlPoints = 5;

    // size of dots (points in graph)
    private final int sizeOfPoint = 25;

    // point used for aproximation
    private int[] x_points = new int[nControlPoints];
    private int[] y_points = new int[nControlPoints];
    private Ellipse2D[] ellipses = new Ellipse2D[nControlPoints];

    regimeOfPlotting regime = regimeOfPlotting.CONTROL_POINTS;
    // index of edited point
    private int draggedIndex = -1;

    // point of the function
    private int[] displayedFunction = new int[256];


    public CSPlot()
    {
        initialize();
        computeControlPoints();


        addMouseMotionListener(this);
        addMouseListener(this);
        setSize(WIDTH, HEIGHT);
    }

    private int linearFunction(int value)
    {
        return value;
    }

    private void initialize()
    {
        int i = 0;
        for (i = 0; i < 256; i += 1)
        {
            displayedFunction[i] = (int) i;
        }
    }

    public void setFunction(int[] function)
    {
        if (function.length != 255)
        {
            return;
        }
        displayedFunction = function;
    }

    public void setFunction(generalMethodHolder function, double lambda)
    {
        lambda = function.transformLambda(lambda);
        for (int i = 0; i < 256; i++)
        {
            int tmp = function.useFunction(i, lambda);
            if (tmp > 255)
            {
                tmp = 255;
            }
            if (tmp < 0)
            {
                tmp = 0;
            }
            displayedFunction[i] = tmp;

            System.out.println("New value with index: " + i + " value: " + tmp);
        }
        computeControlPoints();
        repaint();
    }

    /**
     * interpolation of function display on the graph (approximation by sum of line)
     */
    public int[] sampleLine()
    {
        int[] samples = new int[256];
        int nControlPoints = x_points.length;
        int index = 0;

        for (int i = 0; i < nControlPoints - 1; i++)
        {
            float x1 = x_points[i];
            float y1 = y_points[i];
            float x2 = x_points[i + 1];
            float y2 = y_points[i + 1];

            float a = (y2 - y1) / (x2 - x1); // deferential

            while (index < x2 && index < 256)
            {
                float value = y1 + a * (index - x1);
                if (value < 0)
                {
                    value = 0;
                } else if (value > 255)
                {
                    value = 255;
                }
                samples[index] = (int) value; // rounding down
                index++;
            }
        }

        return samples;
    }

    /**
     * Compute new manipulation points of the new function
     * todo change name
     */
    private void computeControlPoints()
    {
        float interval = 255 / (float) (nControlPoints - 1);

        int index = 0;
        for (float i = 0; i < 256; i += interval, index++)
        {
            x_points[index] = (int) i;
            y_points[index] = displayedFunction[(int) i];
            System.out.println("Index: " + index + " i: " + i + "value: " + displayedFunction[(int) i]);


            ellipses[index] = new Ellipse2D.Double(x_points[index] - ((double) sizeOfPoint / 2), 255 - y_points[index] - ((double) sizeOfPoint / 2), sizeOfPoint, sizeOfPoint);
        }
    }

    public void setNControlPoints(int nControlPoints)
    {
        if (nControlPoints == this.nControlPoints)
        {
            return;
        }
        if (nControlPoints < 3)
        {
            return;
        }
        x_points = new int[nControlPoints];
        y_points = new int[nControlPoints];
        ellipses = new Ellipse2D[nControlPoints];
        this.nControlPoints = nControlPoints;
        computeControlPoints();
        repaint();

    }

    enum regimeOfPlotting
    {
        CONTROL_POINTS, ACCURATE,

    }

    public void setRegime(regimeOfPlotting r)
    {
        if (r == regime)
        {
            return;
        }

        regime = r;

        repaint();
    }

    public byte[] returnLUT()
    {
        byte[] lut = new byte[256];

        if (regime == regimeOfPlotting.ACCURATE)
        {
            for (int i = 0; i < 256; i++)
            {
                lut[i] = (byte) displayedFunction[i];
            }
        } else if (regime == regimeOfPlotting.CONTROL_POINTS)
        {
            int[] samples = sampleLine();
            for (int i = 0; i < 256; i++)
            {
                lut[i] = (byte) samples[i];
                System.out.println("LUT: " + (lut[i] & 0xFF));
            }

        }
        return lut;
    }

    @Override
    public void paint(Graphics g)
    {
        Graphics2D g2 = (Graphics2D) g;

        // Draw background
        g2.setColor(Color.white);
        g2.fillRect(1, 1, WIDTH, HEIGHT);

        // frame
        g2.setColor(Color.black);
        g.drawRect(1, 1, WIDTH - 2, HEIGHT - 2);

        if (regime == regimeOfPlotting.ACCURATE)
        {
            int[] xPoints = new int[WIDTH];
            int[] yPoints = new int[HEIGHT];
            for (int i = 0; i < WIDTH; i++)
            {
                xPoints[i] = i;
                yPoints[i] = 255 - displayedFunction[i];
            }

            g2.setColor(Color.blue);
            g2.drawPolyline(xPoints, yPoints, WIDTH);
        } else
        {

            // Draw control points and lines
            g2.setColor(Color.black);
            for (int i = 0; i < nControlPoints - 1; i++)
            {
                g2.drawLine(x_points[i], 255 - y_points[i], x_points[i + 1], 255 - y_points[i + 1]);
            }

            g2.setColor(Color.red);

            for (Ellipse2D ellipse : ellipses)
            {
                g2.fill(ellipse);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        int x = e.getX();
        int y = e.getY();
        for (int i = 0; i < nControlPoints; i++)
        {
            if (ellipses[i].contains(x, y))
            {
                draggedIndex = i;
                return;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        draggedIndex = -1;
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        //updated the position
        Contrast_stretching.labelminmax.setText("(" + e.getX() + "," + (255 - e.getY()) + ")");

        if (draggedIndex != -1)
        {
            int x = e.getX();
            int y = e.getY();


            // don't allow to change order of point
            if (draggedIndex > 0 && x_points[draggedIndex - 1] > x)
            {
                return;
            }
            if (draggedIndex != (this.nControlPoints - 1) && x_points[draggedIndex + 1] < x)
            {
                return;
            }
            // todo not done :)


            // for saving desired type of function
            float interval = 255 / (float) (nControlPoints - 1);
            displayedFunction[(int) interval * draggedIndex] = 255 - y;

            x_points[draggedIndex] = Math.max(0, Math.min(WIDTH, x));
            y_points[draggedIndex] = 255 - Math.max(0, Math.min(HEIGHT, y));
            ellipses[draggedIndex].setFrame(x_points[draggedIndex] - ((double) sizeOfPoint / 2), 255 - y_points[draggedIndex] - ((double) sizeOfPoint / 2), sizeOfPoint, sizeOfPoint);
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        Contrast_stretching.labelminmax.setText("(" + e.getX() + "," + (255 - e.getY()) + ")");
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
    }
}


interface transformationFunction
{
    double apply(double value, double lamda);
}

interface transformationOfLambda
{
    double apply(double value);
}

class generalMethodHolder
{
    String name;
    transformationFunction function;
    transformationOfLambda tranLambda;

    /**
     * Holder for the method of transformation. The name is used for display, function is the function which let's create the transformation (same is use for the graph)
     * and tranLambda is the function that transfers the lambda to the right value for the function (can just pass lambda without any change)
     *
     * @param name       Name of the method (used for display)
     * @param function   Function to apply (should be in the range 0-1) return value should be in the range 0-1
     * @param tranLambda Function to apply on lambda (it's applied on only one time )
     */
    generalMethodHolder(String name, transformationFunction function, transformationOfLambda tranLambda)
    {
        this.name = name;
        this.function = function;
        this.tranLambda = tranLambda;
    }

    /**
     * Return the name of the method
     *
     * @return name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Apply the function on the value (param lamda) and return the value in the range 0-255
     *
     * @param value value to apply the function
     * @param lamda lambda to apply the function
     * @return
     */
    int useFunction(double value, double lamda)
    {
        return (int) (255 * function.apply(value / 255.0, lamda));
    }

    /**
     * Apply the function on the lambda and return the value -- can be usable for transforming the lambda to the right value (base value is 0-100)
     *
     * @param value
     * @return
     */
    double transformLambda(double value)
    {
        return tranLambda.apply(value);
    }

}
