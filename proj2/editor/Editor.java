package editor;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.collections.Buffer;
import sun.awt.SunHints;
import sun.awt.image.ImageWatched;

import java.io.*;
import java.util.*;


public class Editor extends Application {


    private static LinkedListDeque<Text> words;
    private static Rectangle cursor;
    private static ScrollBar scroller;
    private static Group root;
    private static int windowWidth = 500;
    private static int windowHeight = 500;
    private static int lineNum;
    private static Text displayText;
    private static int fontSize = 12;
    private static String fontName = "Verdana";
    private static int MARGIN = 5;
    private static int changeY;
    private static String file;
    private static HashMap<Integer, LinkedListDeque<Text>.Node> lines;
    private static int getDimensionInsideMargin(int outsideDimension) {
        return outsideDimension - 2 * MARGIN;
    }
    private static Group textroot;
    private static int stackSize;
    private static Stack<KeyEvent> undos;
    private static Stack<Text> objUndos;
    private static Stack<Text> objRedos;
    private static Stack<KeyEvent> redos;


    public Editor(){
        words = new LinkedListDeque<Text>();
        cursor = new Rectangle(1, 0);
        cursor.setX(5);
        cursor.setY(0);
        scroller = new ScrollBar();
        lineNum = 0;
        root = new Group();
        displayText = new Text(250, 250, "");
        displayText.setTextOrigin(VPos.TOP);
        displayText.setFont(Font.font (fontName, fontSize));
        lines = new HashMap<Integer, LinkedListDeque<Text>.Node>();
        textroot = new Group();
        root.getChildren().add(textroot);
        changeY = 0;
        undos  = new Stack<KeyEvent>();
        redos = new Stack<KeyEvent>();
        objRedos = new Stack<Text>();
        objUndos = new Stack<Text>();
        stackSize = 0;
    }

	

	private class KeyEventHandler implements EventHandler<KeyEvent> {
        int textCenterX;
        int textCenterY;


        /** The Text to display on the screen. */



        public KeyEventHandler(Group root, int windowWidth, int windowHeight) {


           
            cursor.setHeight(displayText.getLayoutBounds().getHeight());


            textroot.getChildren().add(displayText);
            textroot.getChildren().add(cursor);
            root.getChildren().add(scroller);
            
        }




        @Override
        public void handle(KeyEvent keyEvent) {
            
            if(keyEvent.isShortcutDown()){
                KeyCode code = keyEvent.getCode();

                if(code == KeyCode.PLUS || code == KeyCode.EQUALS){
                    fontSize += 4;
                    displayText.setFont(Font.font(fontSize));
                    cursor.setHeight(displayText.getLayoutBounds().getHeight());
                    lines.clear();
                    render();
                    objUndos.push(new Text(null));
                    undos.push(keyEvent);
                } else if(code == KeyCode.MINUS){
                    if(fontSize>4){
                        fontSize -= 4;
                        displayText.setFont(Font.font(fontSize));
                        cursor.setHeight(displayText.getLayoutBounds().getHeight());
                        lines.clear();
                        render();
                        objUndos.push(new Text(null));
                        undos.push(keyEvent);

                    }

                }else if(code == KeyCode.P){
                    System.out.println("" + (int)cursor.getX() + ", " + (int)cursor.getY());
                }else if(code == KeyCode.Z){
                    undo();
                    render();
                }else if(code == KeyCode.Y){
                    if(redos.peek()!=null){
                        redo();
                        render();
                    }
                }

            }

            else if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
                // Use the KEY_TYPED event rather than KEY_PRESSED for letter keys, because with
                // the KEY_TYPED event, javafx handles the "Shift" key and associated
                // capitalization.
                String characterTyped = keyEvent.getCharacter();

                if(characterTyped.length() > 0 && characterTyped.charAt(0) == '\r'){
                    Text newLet = new Text("\n");
                    int x = 5;
                    if(!words.isEmpty() && words.first.item != null) {
                        x = (int) Math.round(words.getCurrent().getX() + words.getCurrent().getLayoutBounds().getWidth());
                    }
                    newLet.setFont(Font.font(fontName, fontSize));

                    words.addCurrent(newLet);

                    textroot.getChildren().add(newLet);
                    render();
                    realignADD(newLet);
                    objUndos.push(newLet);
                    undos.push(keyEvent);

                    keyEvent.consume();
                }

                else if(characterTyped.length() > 0 && characterTyped.charAt(0) != 8 && characterTyped.length() > 0) {
                    Text newLet = new Text(characterTyped);

                    words.addCurrent(newLet);
                    textroot.getChildren().add(newLet);
                    render();
                    undos.push(keyEvent);
                    objUndos.push(newLet);
                    keyEvent.consume();
                }


            } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                // Arrow keys should be processed using the KEY_PRESSED event, because KEY_PRESSED
                // events have a code that we can check (KEY_TYPED events don't have an associated
                // KeyCode).

                KeyCode code = keyEvent.getCode();
                /**
                 * Error if deleted to beginning, but still characters after cursor and try to add
                 * occurs with x in adding
                 * */
                if (keyEvent.getCode() == KeyCode.BACK_SPACE || keyEvent.getCode() == KeyCode.DELETE){
                    Text rem = words.removeCurrent();
                    textroot.getChildren().remove(rem);
                    cursorReposition();
                    lines.clear();
                    render();
                    undos.push(keyEvent);
                    objUndos.push(rem);
                    keyEvent.consume();
                }
                else if(code == KeyCode.LEFT){
                    words.moveLeft();
                    cursorReposition();
                }
                else if(code == KeyCode.RIGHT){
                    words.moveRight();
                    cursorReposition();
                }
                else if (code == KeyCode.UP) {
                    up();

                } else if (code == KeyCode.DOWN) {
                    down();
                }
            }
        }


        private void undo(){
            KeyEvent key = undos.pop();

            if(key.getEventType() == KeyEvent.KEY_TYPED){
                Text rem = words.removeCurrent();
                textroot.getChildren().remove(rem);
                cursorReposition();
                lines.clear();
                render();
                objRedos.push(objUndos.pop());
                key.consume();
            }else if(key.getEventType() == KeyEvent.KEY_PRESSED){
                if (key.getCode() == KeyCode.BACK_SPACE || key.getCode() == KeyCode.DELETE){
                    Text t = objUndos.pop();
                    objRedos.push(t);
                    words.addCurrent(t);
                    textroot.getChildren().add(t);
                    render();
                    key.consume();
                }
            }
        }

        private void redo(){
            KeyEvent key = redos.pop();

            if(key.getEventType() == KeyEvent.KEY_TYPED){
                Text rem = words.removeCurrent();
                textroot.getChildren().remove(rem);
                cursorReposition();
                lines.clear();
                render();
//                objRedos.push(objRedos.pop());
                key.consume();
            }else if(key.getEventType() == KeyEvent.KEY_PRESSED){
                if (key.getCode() == KeyCode.BACK_SPACE || key.getCode() == KeyCode.DELETE){
                    Text t = objRedos.pop();
//                    objRedos.push(t);
                    words.addCurrent(t);
                    textroot.getChildren().add(t);
                    render();
                    key.consume();
                }
            }
        }
        private void realignADD(Text t) {


            if(words.current.next != words.last && t!=null){
                int wid = (int)Math.round(t.getLayoutBounds().getWidth());

            }
        }

        private void realignDELETE(Text t) {
            if (words.current.next != null && t != null) {
                int wid = (int)Math.round(t.getLayoutBounds().getWidth());
                LinkedListDeque<Text>.Node nex = words.current.next;
                while (nex != words.last) {
                    int x = (int) Math.round(nex.item.getX() - wid);
                    nex.item.setX(x);
                    nex = nex.next;

                }
            }
        }

        private void up(){
            int x = (int)cursor.getX();

            if((int)cursor.getY() != 0){
                LinkedListDeque<Text>.Node n = words.current.prev;
                while(n.item.getY() == cursor.getY()){n = n.prev;}

                while(n != words.first){
                    int diff = (int)Math.abs(x-n.item.getX()-n.item.getLayoutBounds().getWidth());
                    if(diff > (int)Math.abs(x-n.prev.item.getX()-n.prev.item.getLayoutBounds().getWidth())){ // || n.item.getY() != (cursor.getY()-cursor.getHeight())
                        n = n.prev;
                    }else{
                        break;
                    }
                }
                if(n == words.first){
                    cursor.setX(5);
                    cursor.setY(0);
                    words.moveCurrent(words.first);
                }else {
                    cursor.setX(n.item.getX() + n.item.getLayoutBounds().getWidth());
                    cursor.setY(n.item.getY());
                    words.moveCurrent(n);
                }
            }

        }

        public void down(){
            int x = (int)cursor.getX();

            if((int)cursor.getY() != words.last.prev.item.getY()){
                LinkedListDeque<Text>.Node n = words.current.next;
                while(n.item.getY() == cursor.getY()){n = n.next;}

                while(n.next != words.last){
                    int diff = (int)Math.abs(x-n.item.getX()-n.item.getLayoutBounds().getWidth());
                    if(diff > (int)Math.abs(x-n.next.item.getX()-n.next.item.getLayoutBounds().getWidth())){ // || n.item.getY() != (cursor.getY()-cursor.getHeight())
                        n = n.next;
                    }else{
                        break;
                    }
                }
                if(n == words.last){
                    cursor.setX(words.last.prev.item.getX()+words.last.prev.item.getLayoutBounds().getWidth());
                    cursor.setY(words.last.prev.item.getY());
                    words.moveCurrent(words.last.prev);
                }else {
                    cursor.setX(n.item.getX() + n.item.getLayoutBounds().getWidth());
                    cursor.setY(n.item.getY());
                    words.moveCurrent(n);
                }
            }
        }

        private void cursorReposition() {

            if (words.first.item == null) {
                cursor.setX(5);
                cursor.setY(0);
            }
            else if(words.current.prev == words.first){
                cursor.setX((int)Math.round(words.first.item.getLayoutBounds().getWidth())+5);
                cursor.setY((int)Math.round(words.first.item.getY()));
            }
            else{
                cursor.setX((int)Math.round(words.current.prev.item.getX() + words.current.prev.item.getLayoutBounds().getWidth()));
                cursor.setY((int)Math.round(words.current.prev.item.getY()));

            }
        }

    }


    //NEED TO ACCOUNT FOR SOMETHING DELETED IN FIRST SPACE --> DOESNT MOVE
    public static void render(){
        LinkedListDeque<Text>.Node n;


        if(words.first.item != null){

            n = words.first;
            lines.put(0, words.first);

            displayText.setFont(Font.font(fontName, fontSize));
            cursor.setHeight(displayText.getLayoutBounds().getHeight());

            int x = 5;
            int y = 0;
            n.item.setFont(Font.font(fontName, fontSize));

            while(n != words.last){
                n.item.setFont(Font.font(fontName, fontSize));


                if(n == words.first){
                    x = 5;
                    y = 0;
                }else if(n.item.getText().equals("\n")){
                    x = 5;
                    y += (int)Math.round(n.item.getLayoutBounds().getHeight()/2.0);
                    lines.put(y, n);
//                        System.out.println(y);
                }else if(n.prev == words.current){
                    x = (int)Math.round(n.prev.prev.item.getX() +
                            n.prev.prev.item.getLayoutBounds().getWidth());
                }else{
                    x = (int)Math.round(n.prev.item.getX() +
                            n.prev.item.getLayoutBounds().getWidth());
                    y = (int)Math.round(n.prev.item.getY());
                }

                //WORD WRAP
                if((x+n.item.getLayoutBounds().getWidth()) > windowWidth-5){
                    if(!n.item.getText().equals(" ")) {
                        n = spaceParse(n).next;
                        if(n == words.current){
                            n = n.next;
                        }
                        x = 5;
                        y += n.item.getLayoutBounds().getHeight();
                        lines.put(y, n);
                    }else{
                        x = windowWidth-5;
                    }
                }

                if(y > windowHeight){
                    textroot.setLayoutY(-(int)Math.round((int)Math.round(n.item.getLayoutBounds().getHeight()))*
                            ((y-windowHeight)/n.item.getLayoutBounds().getHeight()));

                }

                n.item.setX(x);
                n.item.setY(y);
                n.item.setTextOrigin(VPos.TOP);
                n = n.next;


                cursor.setX(words.current.prev.item.getX() +
                        words.current.prev.item.getLayoutBounds().getWidth());
                cursor.setY(words.current.prev.item.getY());
                if(n == words.current){
                    n = n.next;
                }
            }
            System.out.println(lines.keySet());
            for(int k : lines.keySet()){
                System.out.println(lines.get(k).item.getText());
            }
            makeScroller();

        }
    }

    public static LinkedListDeque<Text>.Node spaceParse(LinkedListDeque<Text>.Node endLet){
        LinkedListDeque<Text>.Node spaceNode = endLet;

        while(!spaceNode.item.getText().equals(" ")){
            if(spaceNode.item.getX() == 5 || spaceNode == words.first){
                System.out.println("HIT?");
                return endLet.prev;
            }
            spaceNode = spaceNode.prev;
            if(spaceNode == words.current){
                spaceNode = spaceNode.prev;
            }
        }
        return spaceNode;
    }

    public static void makeScroller(){
        scroller.setOrientation(Orientation.VERTICAL);
        scroller.setPrefHeight(windowHeight); //change this val
        scroller.setLayoutX(windowWidth-scroller.getLayoutBounds().getWidth()+5); //change this val
        scroller.setMin(0);
        scroller.setMax(windowHeight);
        // make a min/max
        


    }

    private class MouseClickEventHandler implements EventHandler<MouseEvent> {
        /** A Text object that will be used to print the current mouse position. */
        @Override
        public void handle(MouseEvent mouseEvent) {
            // Because we registered this EventHandler using setOnMouseClicked, it will only called
            // with mouse events of type MouseEvent.MOUSE_CLICKED.  A mouse clicked event is
            // generated anytime the mouse is pressed and released on the same JavaFX node.
            double mousePressedX = mouseEvent.getX();
            double mousePressedY = mouseEvent.getY()+changeY;

            System.out.print(mousePressedX);
            System.out.print(", ");
            System.out.println(mousePressedY);

            if(words.first.item !=null && (int)Math.abs((int)mousePressedX-words.first.item.getX()) <
                    (int)words.first.item.getLayoutBounds().getWidth() && (int)mousePressedY <
                    words.first.item.getLayoutBounds().getHeight()){
                cursor.setX(5);
                cursor.setY(0);
                words.moveCurrent(words.first);
                return;
            }

            LinkedListDeque<Text>.Node n = words.first;
            int yPos = 0;
            Set<Integer> keys = lines.keySet();

                Iterator<Integer> iterator = keys.iterator();
                //need to compare front and back values to find right line
                //also need to change cursor node according to mouse click

            System.out.println((int)Math.round(displayText.getLayoutBounds().getHeight()*
                    (int)(mousePressedY/displayText.getLayoutBounds().getHeight())));
            System.out.println((int)(mousePressedY/displayText.getLayoutBounds().getHeight()));
            System.out.println((int)Math.round(displayText.getLayoutBounds().getHeight()));
            n = lines.get((int)Math.round(displayText.getLayoutBounds().getHeight())*
                    (int)(mousePressedY/displayText.getLayoutBounds().getHeight()));
            yPos = (int)Math.round(displayText.getLayoutBounds().getHeight())*
                    (int)(mousePressedY/displayText.getLayoutBounds().getHeight());
            int diff = (int)Math.abs(mousePressedX - n.item.getX() - n.item.getLayoutBounds().getWidth());
            while(n.next != words.last){

                if(n.next == words.current && (words.current.next == words.last || words.current.next.item.getY() != yPos)){
                    break;
                }else if(n.next == words.current){
                    if(diff > (int)Math.abs(mousePressedX-n.next.next.item.getX() - n.next.next.item.getLayoutBounds().getWidth())){
                        n = n.next.next;
                        diff = (int)Math.abs(mousePressedX - n.item.getX() - n.item.getLayoutBounds().getWidth());
                    }else{
                        break;
                    }
                }else if(n.next == words.last){
                    break;
                }else if(diff > (int)Math.abs(mousePressedX - n.next.item.getX() -
                    n.next.item.getLayoutBounds().getWidth()) && n.item.getY() == yPos) {

                    n = n.next;
                    diff = (int) Math.abs(mousePressedX - n.item.getX() - n.item.getLayoutBounds().getWidth());
                }else{
                    break;
                }

                }

            cursor.setX((int)(Math.round(n.item.getX() + n.item.getLayoutBounds().getWidth())));
            cursor.setY(n.item.getY());

            if(n==words.first) {
                words.moveCurrent(n.next);
            }else{
                words.moveCurrent(n);
            }


        }


    }



    private class CursorBlinkEventHandler implements EventHandler<ActionEvent> {
        private int currentColorIndex = 0;
        private Color[] boxColors =
                {Color.BLACK, Color.TRANSPARENT};

        CursorBlinkEventHandler() {
            // Set the color to be the first color in the list.
            changeColor();
        }

        private void changeColor() {
            cursor.setFill(boxColors[currentColorIndex]);
            currentColorIndex = (currentColorIndex + 1) % boxColors.length;
        }



        @Override
        public void handle(ActionEvent event) {
            changeColor();
        }
    }

    public void makeCursorColorChange() {
            // Create a Timeline that will call the "handle" function of RectangleBlinkEventHandler
            // every 1 second.
            final Timeline timeline = new Timeline();
            // The rectangle should continue blinking forever.
            timeline.setCycleCount(Timeline.INDEFINITE);
            CursorBlinkEventHandler cursorChange = new CursorBlinkEventHandler();
            KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), cursorChange);
            timeline.getKeyFrames().add(keyFrame);
            timeline.play();
        }


    @Override
    public void start(Stage primaryStage) {
         // Create a Node that will be the parent of all things displayed on the screen.
        // The Scene represents the window: its height and width will be the height and width
        // of the window displayed.
        Scene scene = new Scene(root, windowWidth, windowHeight, Color.WHITE);

        // To get information about what keys the user is pressing, create an EventHandler.
        // EventHandler subclasses must override the "handle" function, which will be called
        // by javafx.
        EventHandler<KeyEvent> keyEventHandler =
                new KeyEventHandler(root, windowWidth, windowHeight);
        // Register the event handler to be called for all KEY_PRESSED and KEY_TYPED events.
        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);
        scene.setOnMouseClicked(new MouseClickEventHandler());
        makeCursorColorChange();
        makeScroller();

        //adjust so it works with clicking!!!!!!
        scroller.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldValue,
                    Number newValue) {
                // newValue describes the value of the new position of the scroll bar. The numerical
                // value of the position is based on the position of the scroll bar, and on the min
                // and max we set above. For example, if the scroll bar is exactly in the middle of
                // the scroll area, the position will be:
                //      scroll minimum + (scroll maximum - scroll minimum) / 2
                // Here, we can directly use the value of the scroll bar to set the height of Josh,
                // because of how we set the minimum and maximum above.
            textroot.setLayoutY((int)Math.round(-(int)Math.round(newValue.doubleValue())));
            changeY = (int)Math.round(newValue.doubleValue());
            render();
            }
        });

        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenWidth,
                    Number newScreenWidth) {
                windowWidth = getDimensionInsideMargin(newScreenWidth.intValue());
                makeScroller();
                render();
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenHeight,
                    Number newScreenHeight) {
                windowHeight = getDimensionInsideMargin(newScreenHeight.intValue());
                makeScroller();
                render();
            }
        });




        primaryStage.setTitle("Editor");
        // This is boilerplate, necessary to setup the window where things are displayed.
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {

//        if(args.length == 0){
//            System.out.println("No filename provided");
//            System.exit(0);
//        }else if(args.length > 2){
//            System.out.println("Too many input arguments");
//            System.exit(0);
//        }else{
//            String filename = args[0];
//            if (args.length == 2) {
//                String str = (args[1]);
//            }
//            try{
//                file = filename;
//                File input = new File(filename);
//                if(input.isDirectory()){
//                    return;
//                }else if(!input.exists()){
//                    FileWriter writer = new FileWriter(filename);
//                    writer.close();
//                }else if(input.exists()){
//                    FileReader reader = new FileReader(input);
//                    BufferedReader buffer = new BufferedReader(reader);
//
//                    int intRead = -1;
//
//                    while((intRead = buffer.read()) != -1){
//                        char charac = (char) intRead;
//                        Text t = new Text("" + charac);
//                        t.setFont(Font.font(fontName, fontSize));
//                        t.setTextOrigin(VPos.TOP);
//                        words.addCurrent(t);
//                        textroot.getChildren().add(t);
//                        render();
//
//                    }
//                }
//            }catch (FileNotFoundException fileNotFoundException) {
//                System.out.println("File not found! Exception was: " + fileNotFoundException);
//            }catch (IOException ioException) {
//                System.out.println("Error when copying; exception was: " + ioException);
//            }
//        }
        launch(args);
    }
}