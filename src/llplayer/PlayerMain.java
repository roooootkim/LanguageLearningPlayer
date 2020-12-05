package llplayer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

public class PlayerMain {

	static PlayerMain thisApp;

	private final JFrame frame;

	private final EmbeddedMediaPlayerComponent mediaPlayerComponent;

	private JButton playButton;
	private JButton pauseButton;
	private JButton stopButton;
	private JButton rewindButton;
	private JButton skipButton;
    
	private String mediaFilePath;
    private String subtitleFilePath;
    
    JTextField[] scriptField;
    Timer programTimer;
    
    private static final int subNum = 2;
    private Sami[] subtitle;

    public static void main(String[] args) {
        thisApp = new PlayerMain();
    }
    
    void createMenu() {
    	JMenuBar menuBar = new JMenuBar();
    	JMenu fileMenu = new JMenu("Menu");
    	JMenuItem openMedia = new JMenuItem("Media Open");
    	JMenuItem[] openSubtitle = new JMenuItem[subNum];
    	
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
                subtitle[0] = null;
                subtitle[1] = null;
                
                subtitleFilePath = mediaFilePath.substring(0, mediaFilePath.length() - 3).concat("smi");
                try {
					subtitle[0] = new Sami(subtitleFilePath);
        		} catch (IOException error) {
        			//default subtitle file doesn't exist;
        			subtitle[0] = null;
        		}
            }
        });
    	
    	for(int i = 0; i < subNum; i++) {
    		int num = i;
    		openSubtitle[i] = new JMenuItem("Subtitle" + (num + 1) + " Open");
        	openSubtitle[i].addActionListener(new ActionListener() {
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
    					subtitle[num] = new Sami(subtitleFilePath);
    				} catch (IOException error) {
            			//subtitle file doesn't exist;
            			subtitle[num] = null;
    				}
                }
            });
    	}
    	
    	fileMenu.add(openMedia);
    	for(int i = 0; i < subNum; i++)
    		fileMenu.add(openSubtitle[i]);
    	menuBar.add(fileMenu);
    	frame.setJMenuBar(menuBar);
    }
    
    JPanel createControlsPane() {
        JPanel controlsPane = new JPanel();
        playButton = new JButton("Play");
        controlsPane.add(playButton);
        pauseButton = new JButton("Pause");
        controlsPane.add(pauseButton);
        stopButton = new JButton("Stop");
        controlsPane.add(stopButton);
        rewindButton = new JButton("Rewind");
        controlsPane.add(rewindButton);
        skipButton = new JButton("Skip");
        controlsPane.add(skipButton);

        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mediaPlayerComponent.mediaPlayer().controls().play();
            }
        });
        
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mediaPlayerComponent.mediaPlayer().controls().pause();
            }
        });
        
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mediaPlayerComponent.mediaPlayer().controls().stop();
            }
        });

        rewindButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if(subtitle[0] == null) 
                    mediaPlayerComponent.mediaPlayer().controls().skipTime(-10000);
            	else {
                	long prevTime = subtitle[0].getPrevTime();
                	long curTime = mediaPlayerComponent.mediaPlayer().status().time();
                    mediaPlayerComponent.mediaPlayer().controls().skipTime(prevTime - curTime);
            	}
            }
        });

        skipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if(subtitle[0] == null) 
                    mediaPlayerComponent.mediaPlayer().controls().skipTime(-10000);
            	else {
                	long nextTime = subtitle[0].getNextTime();
                	long curTime = mediaPlayerComponent.mediaPlayer().status().time();
                    mediaPlayerComponent.mediaPlayer().controls().skipTime(nextTime - curTime);
            	}
            }
        });
        
        return controlsPane;
    }
    
    JPanel createSubtitlePane(int num) {
    	JPanel subtitlePane = new JPanel();
    	subtitlePane.setLayout(new GridLayout());
        scriptField[num] = new JTextField();
        scriptField[num].setText("subtitle " + (num + 1));
        subtitlePane.add(scriptField[num]);
        
        return subtitlePane;
    }

    public PlayerMain() {
        frame = new JFrame("Language Learning Player");
        frame.setBounds(100, 100, 1280, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mediaPlayerComponent.release();
                System.exit(0);
            }
        });
        
        subtitle = new Sami[2];
        scriptField = new JTextField[2];
        
        //create Menu Bar;
        createMenu();
        
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());

        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        contentPane.add(mediaPlayerComponent, BorderLayout.CENTER);
        
        JPanel southPane = new JPanel();
        southPane.setLayout(new GridLayout(0, 1));
        
        for(int i = 0; i < subNum; i++) {
            JPanel subtitlePane = createSubtitlePane(i);
            southPane.add(subtitlePane);
        }
        
        JPanel controlsPane = createControlsPane();
        southPane.add(controlsPane);
        
        
        contentPane.add(southPane, BorderLayout.SOUTH);

        frame.setContentPane(contentPane);
        frame.setVisible(true);
        
        programTimer = new Timer(1, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if(!mediaPlayerComponent.mediaPlayer().status().isPlaying()) return;
            	for(int i = 0; i < subNum; i++) {
            		if(subtitle[i] != null) {
            			scriptField[i].setText(subtitle[i].getScript(mediaPlayerComponent.mediaPlayer().status().time()));
            		}
            	}
            }
        });
        programTimer.start();
    }
}