package llplayer;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

public class PlayerMain {

	static PlayerMain thisApp;


	private final JFrame frame;

	private final EmbeddedMediaPlayerComponent mediaPlayerComponent;

	private final JButton pauseButton;
	private final JButton rewindButton;
	private final JButton skipButton;
    
	private String mediaFilePath;
    private String subtitleFilePath;
    
    private Sami subtitle;

    public static void main(String[] args) {
        thisApp = new PlayerMain();
    }
    
    void createMenu() {
    	JMenuBar menuBar = new JMenuBar();
    	JMenu fileMenu = new JMenu("Menu");
    	JMenuItem openMedia = new JMenuItem("Media Open");
    	JMenuItem openSubtitle = new JMenuItem("Subtitle Open");
    	
    	openMedia.addActionListener(new ActionListener() {
    		JFileChooser chooser;
            public void actionPerformed(ActionEvent e) {
            	chooser = new JFileChooser();
            	FileNameExtensionFilter filter = new FileNameExtensionFilter(
            			"MP4 & AVI Media",
            			"mp4", "avi");
            	chooser.setFileFilter(filter);
            	
            	int ret = chooser.showOpenDialog(null);
            	if(ret != JFileChooser.APPROVE_OPTION) {
            		//파일을 선택하지 않은 경우.
            		return;
            	}
            	
            	mediaFilePath = chooser.getSelectedFile().getPath();
                mediaPlayerComponent.mediaPlayer().media().play(mediaFilePath);
                
                //check default subtitle file.
                subtitleFilePath = mediaFilePath.substring(0, mediaFilePath.length() - 3).concat("smi");
                try {
					subtitle = new Sami(subtitleFilePath);
        		} catch (IOException error) {
        			//default subtitle file doesn't exist;
        			subtitle = null;
        		}
            }
        });
    	
    	openSubtitle.addActionListener(new ActionListener() {
    		JFileChooser chooser;
            public void actionPerformed(ActionEvent e) {
            	chooser = new JFileChooser();
            	FileNameExtensionFilter filter = new FileNameExtensionFilter(
            			"SMI Subtitle",
            			"smi");
            	chooser.setFileFilter(filter);
            	
            	int ret = chooser.showOpenDialog(null);
            	if(ret != JFileChooser.APPROVE_OPTION) {
            		//파일을 선택하지 않은 경우.
            		return;
            	}
            	
            	subtitleFilePath = chooser.getSelectedFile().getPath();
            	try {
					subtitle = new Sami(subtitleFilePath);
				} catch (IOException error) {
        			//subtitle file doesn't exist;
        			subtitle = null;
				}
            }
        });
    	
    	fileMenu.add(openMedia);
    	fileMenu.add(openSubtitle);
    	menuBar.add(fileMenu);
    	frame.setJMenuBar(menuBar);
    }

    public PlayerMain() {
        frame = new JFrame("My First Media Player");
        frame.setBounds(100, 100, 600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mediaPlayerComponent.release();
                System.exit(0);
            }
        });

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());

        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        contentPane.add(mediaPlayerComponent, BorderLayout.CENTER);

        JPanel controlsPane = new JPanel();
        pauseButton = new JButton("Pause");
        controlsPane.add(pauseButton);
        rewindButton = new JButton("Rewind");
        controlsPane.add(rewindButton);
        skipButton = new JButton("Skip");
        controlsPane.add(skipButton);
        contentPane.add(controlsPane, BorderLayout.SOUTH);
        
        createMenu();

        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mediaPlayerComponent.mediaPlayer().controls().pause();
            }
        });

        rewindButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mediaPlayerComponent.mediaPlayer().controls().skipTime(-10000);
            }
        });

        skipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mediaPlayerComponent.mediaPlayer().controls().skipTime(10000);
            }
        });

        frame.setContentPane(contentPane);
        frame.setVisible(true);
    }
}