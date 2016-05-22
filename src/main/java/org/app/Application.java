package org.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.swing.Timer;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.helper.AppleScriptLib;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.datatype.Artwork;
import org.jaudiotagger.tag.reference.GenreTypes;

import com.itunes.ItunesLibrary;
import com.itunes.ItunesTrack;
import com.itunes.parser.ItunesLibraryParser;
import com.itunes.parser.logging.DefaultParserStatusUpdateLogger;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.wb.swt.SWTResourceManager;

public class Application {

	protected Shell shlTangoTuneApp;

	private Display display;
	private Text txtPath;

	private Label lblCount;
	private Label lblCurrent;

	private ItunesLibrary library = null;
	private Integer[] trackIds = null;
	private Integer trackId;
	private Integer trackCount = 0;
	private Integer currentTrack = 1;

	private StyledText txtLyric;
	private StyledText lyricWindowEdit;

	private Button btnPlay;
	private Button btnSave;
	private Button btnPreviouslyrics;
	private Button btnNextLyrics;
	private Label lblSuccessRate;
	private Text txtTitle;
	private Text txtArtist;
	private Text txtAlbumArtist;
	private Text txtAlbum;
	private Text txtComposer;
	private Text txtGrouping;
	private Text txtComment;
	private Text txtGenre;
	private Text txtYear;
	private Text txtTime;
	private Text txtDisc;
	private Text txtTrack;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Application window = new Application();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		display = Display.getDefault();
		new Thread(new Runnable() {
			public void run()
			{
				if (!display.isDisposed()) {
					while (true)
					{
						Display.getDefault().asyncExec(new Runnable() {
							public void run()
							{
								String selTracks = AppleScriptLib.getSelectedTracks();
								String[] stringArray = selTracks.split(",");
								int length = stringArray.length;
								if (!selTracks.equals("") && trackCount != length) {
									for (int i = 0; i < length; i++) {
										trackIds[i] = Integer.parseInt(stringArray[i].trim());
									}
									trackCount = length;
									currentTrack = 1;
									txtLyric.setText(selTracks);
									updateGUI();
								}
							}
						});

						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}).start();
		createContents();
		updateGUI();
		shlTangoTuneApp.open();
		shlTangoTuneApp.layout();
		while (!shlTangoTuneApp.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlTangoTuneApp = new Shell();
		shlTangoTuneApp.setSize(650, 650);
		shlTangoTuneApp.setText("Tango TuneApp");
		shlTangoTuneApp.setLayout(null);

		Label lblPath = new Label(shlTangoTuneApp, SWT.NONE);
		lblPath.setBounds(10, 10, 58, 14);
		lblPath.setText("Library:");

		txtPath = new Text(shlTangoTuneApp, SWT.BORDER);
		txtPath.setEnabled(false);
		txtPath.setEditable(false);
		txtPath.setBounds(76, 7, 439, 19);
		// TODO: if the file location is NULL we cannot analyse the data!
		String libraryLocation = AppleScriptLib.getXmlFileLocatoin();
		if (libraryLocation == null) {
			txtPath.setText("The iTunes Music library could not be identified. Please check your iTunes settings!");
		} else {
			txtPath.setText(libraryLocation);
			DefaultParserStatusUpdateLogger logger = new DefaultParserStatusUpdateLogger(true, System.out);
			library = ItunesLibraryParser.parseLibrary(libraryLocation, logger);
			trackCount = library.getTracks().size();
			trackIds = library.getTracks().keySet().toArray(new Integer[trackCount]);
		}

		Label lblArtwork = new Label(shlTangoTuneApp, SWT.BORDER);
		lblArtwork.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblArtwork.setBounds(175, 315, 120, 120);

		txtLyric = new StyledText(shlTangoTuneApp, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
		txtLyric.setBounds(10, 445, 300, 120);

		lyricWindowEdit = new StyledText(shlTangoTuneApp, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
		lyricWindowEdit.setBounds(320, 445, 300, 120);

		Label lblTitle = new Label(shlTangoTuneApp, SWT.NONE);
		lblTitle.setBounds(10, 30, 150, 14);
		lblTitle.setText("Title");

		Label lblArtist = new Label(shlTangoTuneApp, SWT.NONE);
		lblArtist.setBounds(10, 70, 150, 14);
		lblArtist.setText("Artist");

		Label lblComposer = new Label(shlTangoTuneApp, SWT.NONE);
		lblComposer.setBounds(10, 190, 150, 14);
		lblComposer.setText("Composer");

		Label lblComment = new Label(shlTangoTuneApp, SWT.NONE);
		lblComment.setBounds(10, 270, 150, 14);
		lblComment.setText("Comment");

		Label lblGenre = new Label(shlTangoTuneApp, SWT.NONE);
		lblGenre.setBounds(10, 310, 150, 14);
		lblGenre.setText("Genre");

		Label lblYear = new Label(shlTangoTuneApp, SWT.NONE);
		lblYear.setBounds(10, 350, 70, 14);
		lblYear.setText("Year");

		lblCount = new Label(shlTangoTuneApp, SWT.NONE);
		lblCount.setBounds(126, 582, 84, 14);
		lblCount.setText("Count: " + trackCount.toString());

		lblCurrent = new Label(shlTangoTuneApp, SWT.NONE);
		lblCurrent.setBounds(46, 582, 85, 14);
		lblCurrent.setText("Current: " + currentTrack.toString());

		Button btnFirst = new Button(shlTangoTuneApp, SWT.NONE);
		btnFirst.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (trackCount > 0) {
					currentTrack = 1;
					// currentLyrics=1;
					updateGUI();
				}
			}
		});
		btnFirst.setBounds(0, 602, 50, 28);
		btnFirst.setText("<<");

		Button btnPrevious = new Button(shlTangoTuneApp, SWT.NONE);
		btnPrevious.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (trackCount > 0 && currentTrack > 1) {
					currentTrack--;
					// currentLyrics=1;
					updateGUI();
				}
			}
		});
		btnPrevious.setBounds(46, 602, 80, 28);
		btnPrevious.setText("<");

		Button btnNext = new Button(shlTangoTuneApp, SWT.NONE);
		btnNext.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (trackCount > 0 && currentTrack < trackCount) {
					currentTrack++;
					// currentLyrics=1;
					updateGUI();
				}
			}
		});
		btnNext.setBounds(116, 602, 80, 28);
		btnNext.setText(">");

		Button btnLast = new Button(shlTangoTuneApp, SWT.NONE);
		btnLast.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (trackCount > 0) {
					currentTrack = trackCount;
					// currentLyrics=1;
					updateGUI();
				}
			}
		});
		btnLast.setBounds(202, 602, 50, 28);
		btnLast.setText(">>");

		btnPlay = new Button(shlTangoTuneApp, SWT.NONE);
		btnPlay.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AppleScriptLib.playPauseTrack(trackId);
			}
		});
		btnPlay.setBounds(255, 568, 94, 28);
		btnPlay.setText("Play");

		btnSave = new Button(shlTangoTuneApp, SWT.NONE);
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// updateTrack();
			}
		});
		btnSave.setBounds(255, 602, 94, 28);
		btnSave.setText("Save");

		btnPreviouslyrics = new Button(shlTangoTuneApp, SWT.NONE);
		btnPreviouslyrics.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// if (trackCount>0 && currentLyrics>1) {
				// currentLyrics--;
				// updateGUI();
				// }
			}
		});
		btnPreviouslyrics.setBounds(357, 602, 40, 28);
		btnPreviouslyrics.setText("<");

		btnNextLyrics = new Button(shlTangoTuneApp, SWT.NONE);
		btnNextLyrics.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (trackCount > 0) {
					// if ( currentLyrics<tmp.getLyrics().size() ) {
					// currentLyrics++;
					// updateGUI();
					// }
				}
			}
		});
		btnNextLyrics.setBounds(580, 602, 37, 28);
		btnNextLyrics.setText(">");

		lblSuccessRate = new Label(shlTangoTuneApp, SWT.NONE);
		lblSuccessRate.setBounds(403, 609, 171, 14);
		lblSuccessRate.setText("Success Rate:");

		Menu menu = new Menu(shlTangoTuneApp, SWT.BAR);
		shlTangoTuneApp.setMenuBar(menu);

		MenuItem mntmFile = new MenuItem(menu, SWT.NONE);
		mntmFile.setText("File");

		MenuItem mntmCascade = new MenuItem(menu, SWT.CASCADE);
		mntmCascade.setText("Cascade");

		Menu menu_1 = new Menu(mntmCascade);
		mntmCascade.setMenu(menu_1);

		MenuItem mntmTest_2 = new MenuItem(menu_1, SWT.NONE);
		mntmTest_2.setText("test3");

		Label lblAlbumArtist = new Label(shlTangoTuneApp, SWT.NONE);
		lblAlbumArtist.setText("Album Artist");
		lblAlbumArtist.setBounds(10, 110, 150, 14);

		Label lblAlbum = new Label(shlTangoTuneApp, SWT.NONE);
		lblAlbum.setBounds(10, 150, 150, 14);
		lblAlbum.setText("Album");

		Label lblGrouping = new Label(shlTangoTuneApp, SWT.NONE);
		lblGrouping.setBounds(9, 230, 150, 14);
		lblGrouping.setText("Grouping");

		txtTitle = new Text(shlTangoTuneApp, SWT.BORDER);
		txtTitle.setBounds(10, 45, 300, 19);

		txtArtist = new Text(shlTangoTuneApp, SWT.BORDER);
		txtArtist.setBounds(10, 85, 300, 19);

		txtAlbumArtist = new Text(shlTangoTuneApp, SWT.BORDER);
		txtAlbumArtist.setBounds(10, 125, 300, 19);

		txtAlbum = new Text(shlTangoTuneApp, SWT.BORDER);
		txtAlbum.setBounds(10, 165, 300, 19);

		txtComposer = new Text(shlTangoTuneApp, SWT.BORDER);
		txtComposer.setBounds(10, 205, 300, 19);

		txtGrouping = new Text(shlTangoTuneApp, SWT.BORDER);
		txtGrouping.setBounds(10, 245, 300, 19);

		txtComment = new Text(shlTangoTuneApp, SWT.BORDER);
		txtComment.setBounds(10, 285, 300, 19);

		txtGenre = new Text(shlTangoTuneApp, SWT.BORDER);
		txtGenre.setBounds(10, 325, 150, 19);

		txtYear = new Text(shlTangoTuneApp, SWT.BORDER);
		txtYear.setBounds(10, 365, 70, 19);

		Label lblTime = new Label(shlTangoTuneApp, SWT.NONE);
		lblTime.setBounds(90, 350, 70, 14);
		lblTime.setText("Time");

		txtTime = new Text(shlTangoTuneApp, SWT.BORDER);
		txtTime.setBounds(90, 365, 70, 19);

		Label lblDisc = new Label(shlTangoTuneApp, SWT.NONE);
		lblDisc.setBounds(10, 390, 70, 14);
		lblDisc.setText("Disc");

		Label lblTrack = new Label(shlTangoTuneApp, SWT.NONE);
		lblTrack.setBounds(90, 390, 70, 14);
		lblTrack.setText("Track");

		txtDisc = new Text(shlTangoTuneApp, SWT.BORDER);
		txtDisc.setBounds(10, 405, 70, 19);

		txtTrack = new Text(shlTangoTuneApp, SWT.BORDER);
		txtTrack.setBounds(90, 405, 70, 19);

		Label lblLyrics = new Label(shlTangoTuneApp, SWT.NONE);
		lblLyrics.setBounds(10, 430, 150, 14);
		lblLyrics.setText("Lyrics");

	}

	public void SearchLyrics(String sDir) throws InterruptedException {
	}

	private void updateGUI() {

		trackId = trackIds[currentTrack - 1];
		ItunesTrack track = library.getTrackById(trackId.intValue());

		txtTitle.setText("" + track.getName());
		txtArtist.setText("" + track.getArtist());
		txtAlbumArtist.setText("" + track.getAlbumArtist());
		txtAlbum.setText("" + track.getAlbum());
		txtComposer.setText("" + track.getComposer());
		txtGrouping.setText("" + track.getGrouping());
		txtComment.setText("" + track.getComments());
		txtGenre.setText("" + track.getGenre());
		txtYear.setText(Integer.toString(track.getYear()));
		String totalTime = milliToString(track.getTotalTime());
		txtTime.setText(totalTime);
		txtDisc.setText(track.getDiscNumber() + " of " + track.getDiscCount());
		txtTrack.setText(track.getTrackNumber() + " of " + track.getTrackCount());
		txtLyric.setText(AppleScriptLib.getLyric(trackId));
		// txtLyric.setText(AppleScriptLib.getSelectedTracks());

		// Artwork artwork = track.getArtwork();
		// if (artwork.getBinaryData() != null) {
		// BufferedInputStream inputStreamReader = new BufferedInputStream(new
		// ByteArrayInputStream(artwork.getBinaryData()));
		// ImageData sourceData = new ImageData(inputStreamReader);
		// final Image image = new Image(display, sourceData.scaledTo(100,
		// 100));
		// lblArtwork.setImage(image);
		// } else {
		// lblArtwork.setImage(null);
		// }

		// lyricsWindow.setText("");

		// List<Lyrics> lyrics = track.getLyrics();
		// if (lyrics.size() >= currentLyrics) {
		// lyricsWindow.setText(lyrics.get(currentLyrics-1).getText());
		// lblSuccessRate.setText("SuccessRate: "+lyrics.get(currentLyrics-1).getSuccessRate().toString());
		// }

		lblCount.setText("Count: " + trackCount.toString());
		// lblCountLRC.setText("LRC: " + lrcCount.toString());
		lblCurrent.setText("Current: " + currentTrack.toString());

		display.update();

	}

	private String milliToString(long millis) {
		long min = TimeUnit.MILLISECONDS.toMinutes(millis);// % 60;
		long sec = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
		return String.format("%d:%02d", min, sec);
	}
}
