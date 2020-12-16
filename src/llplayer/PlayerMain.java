package llplayer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

public class PlayerMain {
	static PlayerMain thisApp;

	private final JFrame frame;

	private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
    
	private String mediaFilePath;
    private String subtitleFilePath;

    private JSlider positionSlider;
    private static final int subNum = 2;
    private Subtitles[] subtitle = new Subtitles[2];
    private JTextField[] scriptField = new JTextField[2];
    private Timer programTimer;
    
    private String api_id = "";
    private String api_key = "";
    
    private String dict_input = "";
    private String dict_output = "Please enter Oxford Dictionary API ID & KEY";
    
    private Dictionary dictionary = new Dictionary();

    public static void main(String[] args) {
        thisApp = new PlayerMain();
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
        
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());

        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        contentPane.add(mediaPlayerComponent, BorderLayout.CENTER);
        
        JPanel southPane = new JPanel();
        southPane.setLayout(new GridLayout(0, 1));

        float positionMax = 1000.0f;
        positionSlider = new JSlider();
        positionSlider.setMinimum(0);
        positionSlider.setMaximum((int) positionMax);
        positionSlider.setValue(0);
        southPane.add(positionSlider);
        
        for(int i = 0; i < subNum; i++) {
            JPanel subtitlePane = createSubtitlePane(i);
            southPane.add(subtitlePane);
        }
        
        JPanel controlsPane = createControlsPane();
        southPane.add(controlsPane);
        contentPane.add(southPane, BorderLayout.SOUTH);
        
        JPanel menuPane = createMenu();
        contentPane.add(menuPane, BorderLayout.NORTH);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(200, 0));
        JPanel dictionaryPane = createDictionaryPane();
        JPanel wordBookPane = createWordBook();
        tabbedPane.addTab("Dictionary", dictionaryPane);
        tabbedPane.addTab("Word Book", wordBookPane);
        contentPane.add(tabbedPane, BorderLayout.EAST);
        
        frame.setContentPane(contentPane);
        frame.setVisible(true);
        
        positionSlider.addMouseListener(new MouseAdapter() {
            boolean mousePressedPlaying = false;
            public void mousePressed(MouseEvent e) {
                if(mediaPlayerComponent.mediaPlayer().status().isPlaying()) {
                    mediaPlayerComponent.mediaPlayer().controls().pause();
                    mousePressedPlaying = true;
                }
            	SetSliderPosition();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            	SetSliderPosition();
                if(mousePressedPlaying) {
                    mousePressedPlaying = false;
                    mediaPlayerComponent.mediaPlayer().controls().play();
                }
            }
            
            public void SetSliderPosition() {
                if(!mediaPlayerComponent.mediaPlayer().status().isSeekable()) {
                    return;
                }
                float positionValue = positionSlider.getValue() / positionMax;
                // Avoid end of file freeze-up
                if(positionValue > 0.99f) {
                    positionValue = 0.99f;
                }
                mediaPlayerComponent.mediaPlayer().controls().setPosition(positionValue);
            }
        });
        
        programTimer = new Timer(1, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if(!mediaPlayerComponent.mediaPlayer().status().isPlaying()) return;
            	for(int i = 0; i < subNum; i++) {
            		if(subtitle[i] != null) {
            			scriptField[i].setText(subtitle[i].getScript(mediaPlayerComponent.mediaPlayer().status().time()));
            			int pos = (int)(mediaPlayerComponent.mediaPlayer().status().position() * positionMax);
            			positionSlider.setValue((int) pos);
            		}
            	}
            }
        });
        programTimer.start();
    }
    
    JPanel createControlsPane() {
        JPanel controlsPane = new JPanel();
        FlowLayout fl = new FlowLayout();
        fl.setAlignment(FlowLayout.LEFT);
        controlsPane.setLayout(fl);
        JButton playButton = new JButton("Play");
        controlsPane.add(playButton);
        JButton pauseButton = new JButton("Pause");
        controlsPane.add(pauseButton);
        JButton stopButton = new JButton("Stop");
        controlsPane.add(stopButton);
        JButton rewindButton = new JButton("Rewind");
        controlsPane.add(rewindButton);
        JButton skipButton = new JButton("Skip");
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
            	positionSlider.setValue(0);
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
                    mediaPlayerComponent.mediaPlayer().controls().skipTime(prevTime - curTime + 1);
                    for(int i = 0; i < subNum; i++) {
                    	if(subtitle[i] != null)
                    		scriptField[i].setText(subtitle[i].getScript(mediaPlayerComponent.mediaPlayer().status().time()));
                    }
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
                    mediaPlayerComponent.mediaPlayer().controls().skipTime(nextTime - curTime + 1);
                    for(int i = 0; i < subNum; i++) {
                    	if(subtitle[i] != null)
                    		scriptField[i].setText(subtitle[i].getScript(mediaPlayerComponent.mediaPlayer().status().time()));
                    }
            	}
            }
        });

        return controlsPane;
    }
    
    JPanel createMenu() {
        JPanel menuPane = new JPanel();
        FlowLayout fl = new FlowLayout();
        fl.setAlignment(FlowLayout.LEFT);
        menuPane.setLayout(fl);
        JLabel text = new JLabel("open");
        menuPane.add(text);
        JButton mediaButton = new JButton("Media");
        menuPane.add(mediaButton);
        JButton[] subtitleButton = new JButton[subNum];
        for(int i = 0; i < subNum; i++) {
        	subtitleButton[i] = new JButton("Subtitle" + (i + 1));
        	menuPane.add(subtitleButton[i]);
        }
        JLabel text2 = new JLabel("Oxford Dictionaries api key: ");
        menuPane.add(text2);
        JButton dictionaryKeyButton = new JButton("Enter Key");
        menuPane.add(dictionaryKeyButton);
        
        mediaButton.addActionListener(new ActionListener() {
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
                for(int i = 0; i < subNum; i++) 
                    subtitle[i] = null;
                 
                subtitleFilePath = mediaFilePath.substring(0, mediaFilePath.length() - 3).concat("smi");
                try {
                	subtitle[0] = new Subtitles(subtitleFilePath);
         		} catch (IOException error) {
         			//default subtitle file doesn't exist;
         			subtitle[0] = null;
         		}
            }
        });
         
        for(int i = 0; i < subNum; i++) {
        	int num = i;
        	subtitleButton[i].addActionListener(new ActionListener() {
         		JFileChooser chooser;
                 public void actionPerformed(ActionEvent e) {
                 	chooser = new JFileChooser();
                 	FileNameExtensionFilter filter = new FileNameExtensionFilter(
                 			"SMI & SRT Subtitle",
                 			"smi", "srt");
                 	chooser.setFileFilter(filter);
                 	
                 	int ret = chooser.showOpenDialog(null);
                 	if(ret != JFileChooser.APPROVE_OPTION) {
                 		//파일을 선택하지 않은 경우.
                 		return;
                 	}
                 	
                 	subtitleFilePath = chooser.getSelectedFile().getPath();
                 	try {
     					subtitle[num] = new Subtitles(subtitleFilePath);
     				} catch (IOException error) {
             			//subtitle file doesn't exist;
             			subtitle[num] = null;
     				}
                 }
        	});
     	}
        
        dictionaryKeyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	 createInputWindow();
            }
        });
        return menuPane;
    }
    
    JPanel createSubtitlePane(int num) {
    	JPanel subtitlePane = new JPanel();
    	subtitlePane.setLayout(new GridLayout());
        scriptField[num] = new JTextField();
        scriptField[num].setHorizontalAlignment(JTextField.CENTER);
        scriptField[num].setText("subtitle " + (num + 1));
        subtitlePane.add(scriptField[num]);
        
        return subtitlePane;
    }
    
    JPanel createDictionaryPane() {
    	JPanel dictionaryPane = new JPanel();
    	//GridBagLayout grid = new GridBagLayout();
    	dictionaryPane.setLayout(new GridBagLayout());
    	GridBagConstraints gbc = new GridBagConstraints();
    	JTextField input = new JTextField(dict_input);
    	JTextArea output = new JTextArea(dict_output);
    	output.setLineWrap(true);
    	output.setWrapStyleWord(true);
    	output.setEditable(false);
    	
    	gbc.fill = GridBagConstraints.BOTH;

    	gbc.gridx = 0;
    	gbc.gridy = 0;
    	gbc.weightx = 3.0;
    	
    	dictionaryPane.add(input, gbc);
    	
    	gbc.gridx = 0;
    	gbc.gridy = 1;
    	gbc.weighty = 1.0;
    	
    	dictionaryPane.add(new JScrollPane(output), gbc);
    	
    	input.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	output.setText(dictionary.search(input.getText()));
            }
        });
    	
    	return dictionaryPane;
    }

    JPanel createWordBook() {
    	JPanel wordBookPane = new JPanel();
    	
    	return wordBookPane;
    }
    
    void createInputWindow() {
    	JFrame inputKeyFrame = new JFrame("Key Input");
    	int width = 250;
    	int height = 150;
        inputKeyFrame.setBounds(frame.getX() + (frame.getWidth()/ 2) - (width / 2) , frame.getY() + (frame.getHeight() / 2) - (height / 2), width, height);
        inputKeyFrame.setResizable(false);
        
        JPanel pane = new JPanel();
        pane.setLayout(new GridLayout(0, 1));
        
        JPanel idPane = new JPanel();
        JLabel idLabel = new JLabel("API ID:");
        idPane.add(idLabel);
        JTextField idText = new JTextField(10);
        idText.setText(api_id);
        idPane.add(idText);
        
        JPanel keyPane = new JPanel();
        JLabel keyLabel = new JLabel("API KEY:");
        idText.setText(api_key);
        keyPane.add(keyLabel);
        JTextField keyText = new JTextField(10);
        keyPane.add(keyText);

        
        JPanel buttonPane = new JPanel();
        JButton enterButton = new JButton("Enter");
        buttonPane.add(enterButton);

        pane.add(idPane);
        pane.add(keyPane);
        pane.add(buttonPane);
        
        inputKeyFrame.setContentPane(pane);
        inputKeyFrame.setVisible(true);
        
        enterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	api_id = idText.getText();
            	api_key = keyText.getText();
            	dictionary.setKey(api_id, api_key);
            	inputKeyFrame.setVisible(false);
            }
        });
    }
}