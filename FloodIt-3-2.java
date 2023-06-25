import java.util.ArrayList;
import java.util.Arrays;
import tester.Tester;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Random;

//represents a single square of the game area
interface ICell {
  // determines if this cell has been clicked on
  boolean sameCell(Posn pos, int size, int col, int row);

  // determines if this cell is the same color as the given cell
  boolean sameColor(ICell that);

  // determines if this cell is the same color as the given cell
  boolean sameColorHelp(Cell that);

  // EFFECT: changes the color of this cell to the color of the
  // given cell
  void update(ICell that);

  // EFFECT: changes the color of this cell to the color of the
  // given cell
  void updateHelp(Cell that);

  // EFFECT: updates the flooded status of this cell
  void floodedNow();

}

//represents an empty cell in the game area
//used as a place holder for top, bottom, left, or right
//values for border cells
class MtCell implements ICell {

  // determines if this cell has been clicked on
  public boolean sameCell(Posn pos, int size, int col, int row) {
    return false;
  }

  // determines if this cell is the same color as the given cell
  public boolean sameColor(ICell that) {
    return false;
  }

  // determines if this cell is the same color as the given cell
  public boolean sameColorHelp(Cell that) {
    return false;
  }

  // EFFECT: changes the color of this cell to the color of the
  // given cell
  public void update(ICell that) {
    // does nothing in the empty case
    // must be in interface face though because we use dynamic dispatch
  }

  // EFFECT: changes the color of this cell to the color of the
  // given cell
  public void updateHelp(Cell that) {
    // does nothing in the empty case
    // must be in interface face though because we use dynamic dispatch
  }

  // EFFECT: updates the flooded status of this cell
  public void floodedNow() {
    // this method is never reached thus is empty;
  }
}

//represents a single non empty square of the game area
class Cell implements ICell {
  // In logical coordinates, with the origin at the top-left corner of the screen
  int x;
  int y;
  Color color;
  boolean flooded;
  // the four adjacent cells to this one
  ICell left;
  ICell top;
  ICell right;
  ICell bottom;

  ICell empty = new MtCell();

  // convenience constructor: sets coordinates and color
  Cell(int x, int y, Color color) {
    this.x = x;
    this.y = y;
    this.color = color;
  }

  Cell(int x, int y, Color color, boolean flooded, ICell left, ICell top, ICell right,
      ICell bottom) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
  }

  // produces an image of this cell
  WorldImage renderCell(int size) {
    RectangleImage cell = new RectangleImage(size, size, "solid", this.color);
    return cell;
  }

  // assigns the left, top, right, and bottom values for this cell
  void assign(ArrayList<Cell> others) {
    this.bottom = this.assignBottom(others);
    this.top = this.assignTop(others);
    this.left = this.assignLeft(others);
    this.right = this.assignRight(others);
  }

  // assigns the top value for this cell
  ICell assignTop(ArrayList<Cell> others) {
    if (this.y == 0 || (others.indexOf(this) - (int) Math.sqrt(others.size()) < 0)) {
      return this.empty;
    }
    else {
      return others.get(others.indexOf(this) - (int) Math.sqrt(others.size()));
    }
  }

  // assigns the bottom value for this cell
  ICell assignBottom(ArrayList<Cell> others) {
    if (this.y == others.size()
        || (others.indexOf(this) + (int) Math.sqrt(others.size()) >= others.size())) {
      return this.empty;
    }
    else {
      return others.get(others.indexOf(this) + (int) Math.sqrt(others.size()));
    }
  }

  // assigns the left value for this cell
  ICell assignLeft(ArrayList<Cell> others) {
    if (this.x == 0 || (others.indexOf(this) - 1) < 0) {
      return this.empty;
    }
    else {
      return others.get(others.indexOf(this) - 1);
    }
  }

  // assigns the right value for this cell
  ICell assignRight(ArrayList<Cell> others) {
    if (this.x == others.size() || (others.indexOf(this) + 1) >= others.size()) {
      return this.empty;
    }
    else {
      return others.get(others.indexOf(this) + 1);
    }
  }

  // determines if this cell has been clicked on
  public boolean sameCell(Posn pos, int size, int col, int row) {
    return (pos.x <= (size * (col + 1)) && pos.x >= (this.x + (size * col)))
        && (pos.y <= (size * (row + 1)) && pos.y >= (this.y + (size * row)));
  }

  // determines if this cell is the same color as the given cell
  public boolean sameColor(ICell that) {
    return that.sameColorHelp(this);
  }

  // determines if this cell is the same color as the given cell
  public boolean sameColorHelp(Cell that) {
    return this.color.equals(that.color);
  }

  // EFFECT: changes the color of this cell to the color of the
  // given cell
  public void update(ICell that) {
    that.updateHelp(this);
  }

  // EFFECT: changes the color of this cell to the color of the
  // given cell
  public void updateHelp(Cell that) {
    that.color = this.color;
    if (that.bottom.sameColor(this)) {
      that.bottom.floodedNow();
    }
    if (that.right.sameColor(this)) {
      that.right.floodedNow();
    }
    if (that.left.sameColor(this)) {
      that.left.floodedNow();
    }
    if (that.top.sameColor(this)) {
      that.top.floodedNow();
    }
  }

  // EFFECT: updates the flooded status of this cell
  public void floodedNow() {
    this.flooded = true;
  }

}

//represents the current world state in this flood it game
class FloodItWorld extends World {
  // all of the cells of the game
  ArrayList<Cell> board;
  ArrayList<Color> gameColors;
  ArrayList<Cell> worklist = new ArrayList<Cell>();
  ArrayList<Cell> seen = new ArrayList<Cell>();
  int size;
  // max value allColors.size(); 8
  int colors;
  final static int CANVAS = 500;
  Random rand = new Random();
  // master list of all possible cell colors in this game
  ArrayList<Color> allColors = new ArrayList<Color>(Arrays.asList(Color.red, Color.blue,
      Color.magenta, Color.orange, Color.cyan, Color.pink, Color.green, Color.yellow));
  int cellSize;
  int tries;
  int clicks;
  Posn pos;
  ICell pressedCell;
  int sum;

  FloodItWorld(int size, int colors) {
    this.size = size;
    this.colors = colors;
    this.gameColors = this.genGameColors();
    this.board = this.initBoard();
    this.cellSize = CANVAS / this.size;
    this.tries = (this.size * 5) - this.colors;
    this.pressedCell = new MtCell();
    this.sum = 0;
    this.clicks = 0;
  }

  // convenience constructor: takes in an int that will then be used as
  // a seed for the rand field (for testing purposes)
  FloodItWorld(int seed) {
    this.size = 5;
    this.colors = 8;
    this.rand = new Random(seed);
    this.gameColors = this.genGameColors();
    this.board = this.initBoard();
    this.cellSize = CANVAS / this.size;
    this.pressedCell = new MtCell();
    this.tries = (this.size * 5) - this.colors;
    this.clicks = 0;
  }

  // initializes the game board
  ArrayList<Cell> initBoard() {
    this.board = new ArrayList<Cell>();
    ICell empty = new MtCell();

    for (int y = 0; y < this.size; y++) {
      for (int x = 0; x < this.size; x++) {
        this.board.add(new Cell(x, y, this.genColor(), false, empty, empty, empty, empty));
      }
    }
    this.board.get(0).floodedNow();
    this.assign();
    this.assignFlooded(0);
    this.worklist.add(this.board.get(0));
    return this.board;
  }

  // initializes any flooded cells
  void assignFlooded(int idx) {
    boolean flooded = this.board.get(idx).sameColor(this.board.get(idx).right);
    while (flooded && idx < this.size) {
      this.board.get(idx).right.floodedNow();
      idx++;
    }
    if (this.board.get(idx).bottom.sameColor(this.board.get(idx))) {
      this.board.get(idx).bottom.floodedNow();
      this.assignFlooded(this.board.indexOf(this.board.get(idx).bottom));
    }
  }

  // assigns the top, left, right, and bottom values for each cell
  void assign() {
    for (int i = 0; i < this.board.size(); i++) {
      this.board.get(i).assign(this.board);
    }
  }

  // randomly generates a color from the list of color options
  Color genColor() {
    int num = this.rand.nextInt(this.gameColors.size());
    Color col = this.gameColors.get(num);
    return col;
  }

  // randomly generates a list of colors for this game
  ArrayList<Color> genGameColors() {
    ArrayList<Color> colors = new ArrayList<Color>();
    for (int i = 0; i < this.colors; i++) {
      colors.add(this.allColors.get(i));
    }
    return colors;
  }

  // produces an image of the current world state
  public WorldScene makeScene() {
    WorldScene grid = new WorldScene(0, 0);
    int idx = 0;
    while (idx < Math.pow(this.size, 2)) {
      for (int i = 0; i < this.size; i++) {
        for (int j = 0; j < this.size; j++) {
          grid.placeImageXY(this.board.get(idx).renderCell(cellSize),
              ((this.board.get(idx).x) + (cellSize * j) + (cellSize / 2)),
              ((this.board.get(idx).y) + (cellSize * i) + (cellSize / 2)));
          idx++;
        }
      }
    }
    return grid;
  }

  // EFFECT: updates the world state after a mouse press event
  public void onMousePressed(Posn pos) {
    this.pos = pos;
    this.clicks = this.clicks + 1;
    this.pressedCell = this.findCell(pos);
  }

  // EFFECT: updates the world state after every tick
  public void onTick() {
    if (this.isFlooded() || this.clicks > this.tries) {
      this.gameOver();
    }
    for (int i = 0; i < this.board.size(); i++) {
      if (this.board.get(i).x + this.board.get(i).y == sum) {
        if (this.board.get(i).flooded) {
          this.board.get(i).update(this.pressedCell);
        }
      }
    }
    if (sum < ((this.size + this.size) - 1)) {
      sum++;
    }
    else {
      sum = 0;
    }
  }

  // determines if this game has ended
  void gameOver() {
    if (this.clicks <= this.tries) {
      this.endOfWorld("WINNER!! :D");
    }
    else if (this.clicks > this.tries) {
      this.endOfWorld("LOSER! :(");
    }
  }

  // determines which cell has been clicked on
  public ICell findCell(Posn pos) {
    int idx = 0;
    while (idx < Math.pow(this.size, 2)) {
      for (int i = 0; i < this.size; i++) {
        for (int j = 0; j < this.size; j++) {
          if (this.board.get(idx).sameCell(pos, this.cellSize, j, i)) {
            return this.board.get(idx);
          }
          idx++;
        }
      }
    }
    return new MtCell();
  }

  public void onKeyEvent(String str) {
    if (str.equals("r")) {
      this.initBoard();
    }
  }

  // determines if the entire board has been flooded
  boolean isFlooded() {
    boolean isFlooded = true;
    while (isFlooded) {
      for (int i = 0; i < this.board.size(); i++) {
        isFlooded = this.board.get(i).flooded;
      }
    }
    return isFlooded;
  }

  // displays the final scene after the game has ended
  public WorldScene lastScene(String msg) {
    WorldScene background = this.getEmptyScene();
    TextImage message;
    if (msg.equals("WINNER!! :D")) {
      message = new TextImage(msg, Color.green);
    }
    else {
      message = new TextImage(msg, Color.red);
    }
    background.placeImageXY(message, CANVAS / 2, CANVAS / 2);
    return background;
  }
}

//examples for all tests and classes that represent a flood it game
class ExamplesFloodIT {
  ICell empty = new MtCell();
  Cell w1c1 = new Cell(0, 0, Color.pink, true, this.empty, this.empty, this.empty, this.empty);
  Cell w1c2 = new Cell(1, 0, Color.cyan, false, this.empty, this.empty, this.empty, this.empty);
  Cell w1c3 = new Cell(2, 0, Color.red, false, this.empty, this.empty, this.empty, this.empty);
  Cell w1c4 = new Cell(3, 0, Color.green, false, this.empty, this.empty, this.empty, this.empty);
  Cell w1c5 = new Cell(4, 0, Color.red, false, this.empty, this.empty, this.empty, this.empty);
  Cell w1c6 = new Cell(0, 1, Color.cyan, false, this.empty, this.empty, this.empty, this.empty);
  Cell w1c7 = new Cell(1, 1, Color.green, false, this.empty, this.empty, this.empty, this.empty);
  Cell w1c8 = new Cell(2, 1, Color.cyan, false, this.empty, this.empty, this.empty, this.empty);
  Cell w1c9 = new Cell(3, 1, Color.blue, false, this.empty, this.empty, this.empty, this.empty);
  Cell w1c10 = new Cell(4, 1, Color.green, false, this.empty, this.empty, this.empty, this.empty);
  Cell w1c11 = new Cell(0, 2, Color.pink, false, this.empty, this.empty, this.empty, this.empty);
  Cell w1c12 = new Cell(1, 2, Color.magenta, false, this.empty, this.empty, this.empty, this.empty);
  Cell w1c13 = new Cell(2, 2, Color.green, false, this.empty, this.empty, this.empty, this.empty);
  Cell w1c14 = new Cell(3, 2, Color.red, false, this.empty, this.empty, this.empty, this.empty);
  Cell w1c15 = new Cell(4, 2, Color.red, false, this.empty, this.empty, this.empty, this.empty);
  Cell w1c16 = new Cell(0, 3, Color.orange, false, this.empty, this.empty, this.empty, this.empty);
  Cell w1c17 = new Cell(1, 3, Color.green, false, this.empty, this.empty, this.empty, this.empty);
  Cell w1c18 = new Cell(2, 3, Color.blue, false, this.empty, this.empty, this.empty, this.empty);
  Cell w1c19 = new Cell(3, 3, Color.yellow, false, this.empty, this.empty, this.empty, this.empty);
  Cell w1c20 = new Cell(4, 3, Color.green, false, this.empty, this.empty, this.empty, this.empty);
  Cell w1c21 = new Cell(0, 4, Color.blue, false, this.empty, this.empty, this.empty, this.empty);
  Cell w1c22 = new Cell(1, 4, Color.pink, false, this.empty, this.empty, this.empty, this.empty);
  Cell w1c23 = new Cell(2, 4, Color.yellow, false, this.empty, this.empty, this.empty, this.empty);
  Cell w1c24 = new Cell(3, 4, Color.cyan, false, this.empty, this.empty, this.empty, this.empty);
  Cell w1c25 = new Cell(4, 4, Color.yellow, false, this.empty, this.empty, this.empty, this.empty);

  ArrayList<Cell> world1Board;
  // example cells

  void initData() {
    // example boards
    this.world1Board = new ArrayList<Cell>(Arrays.asList(this.w1c1, this.w1c2, this.w1c3, this.w1c4,
        this.w1c5, this.w1c6, this.w1c7, this.w1c8, this.w1c9, this.w1c10, this.w1c11, this.w1c12,
        this.w1c13, this.w1c14, this.w1c15, this.w1c16, this.w1c17, this.w1c18, this.w1c19,
        this.w1c20, this.w1c21, this.w1c22, this.w1c23, this.w1c24, this.w1c25));

    this.w1c1.assign(this.world1Board);
    this.w1c2.assign(this.world1Board);
    this.w1c3.assign(this.world1Board);
    this.w1c4.assign(this.world1Board);
    this.w1c5.assign(this.world1Board);
    this.w1c6.assign(this.world1Board);
    this.w1c7.assign(this.world1Board);
    this.w1c8.assign(this.world1Board);
    this.w1c9.assign(this.world1Board);
    this.w1c10.assign(this.world1Board);
    this.w1c11.assign(this.world1Board);
    this.w1c12.assign(this.world1Board);
    this.w1c13.assign(this.world1Board);
    this.w1c14.assign(this.world1Board);
    this.w1c15.assign(this.world1Board);
    this.w1c16.assign(this.world1Board);
    this.w1c17.assign(this.world1Board);
    this.w1c18.assign(this.world1Board);
    this.w1c19.assign(this.world1Board);
    this.w1c20.assign(this.world1Board);
    this.w1c21.assign(this.world1Board);
    this.w1c22.assign(this.world1Board);
    this.w1c23.assign(this.world1Board);
    this.w1c24.assign(this.world1Board);
    this.w1c25.assign(this.world1Board);
  }

  WorldScene ws = new WorldScene(0, 0);

  WorldScene sceneMaker() {

    WorldImage w1c1R = this.w1c1.renderCell(100);
    WorldImage w1c2R = this.w1c2.renderCell(100);
    WorldImage w1c3R = this.w1c3.renderCell(100);
    WorldImage w1c4R = this.w1c4.renderCell(100);
    WorldImage w1c5R = this.w1c5.renderCell(100);
    WorldImage w1c6R = this.w1c6.renderCell(100);
    WorldImage w1c7R = this.w1c7.renderCell(100);
    WorldImage w1c8R = this.w1c8.renderCell(100);
    WorldImage w1c9R = this.w1c9.renderCell(100);
    WorldImage w1c10R = this.w1c10.renderCell(100);
    WorldImage w1c11R = this.w1c11.renderCell(100);
    WorldImage w1c12R = this.w1c12.renderCell(100);
    WorldImage w1c13R = this.w1c13.renderCell(100);
    WorldImage w1c14R = this.w1c14.renderCell(100);
    WorldImage w1c15R = this.w1c15.renderCell(100);
    WorldImage w1c16R = this.w1c16.renderCell(100);
    WorldImage w1c17R = this.w1c17.renderCell(100);
    WorldImage w1c18R = this.w1c18.renderCell(100);
    WorldImage w1c19R = this.w1c19.renderCell(100);
    WorldImage w1c20R = this.w1c20.renderCell(100);
    WorldImage w1c21R = this.w1c21.renderCell(100);
    WorldImage w1c22R = this.w1c22.renderCell(100);
    WorldImage w1c23R = this.w1c23.renderCell(100);
    WorldImage w1c24R = this.w1c24.renderCell(100);
    WorldImage w1c25R = this.w1c25.renderCell(100);

    ws.placeImageXY(w1c1R, 50, 50);
    ws.placeImageXY(w1c2R, 150, 50);
    ws.placeImageXY(w1c3R, 250, 50);
    ws.placeImageXY(w1c4R, 350, 50);
    ws.placeImageXY(w1c5R, 450, 50);
    ws.placeImageXY(w1c6R, 50, 150);
    ws.placeImageXY(w1c7R, 150, 150);
    ws.placeImageXY(w1c8R, 250, 150);
    ws.placeImageXY(w1c9R, 350, 150);
    ws.placeImageXY(w1c10R, 450, 150);
    ws.placeImageXY(w1c11R, 50, 250);
    ws.placeImageXY(w1c12R, 150, 250);
    ws.placeImageXY(w1c13R, 250, 250);
    ws.placeImageXY(w1c14R, 350, 250);
    ws.placeImageXY(w1c15R, 450, 250);
    ws.placeImageXY(w1c16R, 50, 350);
    ws.placeImageXY(w1c17R, 150, 350);
    ws.placeImageXY(w1c18R, 250, 350);
    ws.placeImageXY(w1c19R, 350, 350);
    ws.placeImageXY(w1c20R, 450, 350);
    ws.placeImageXY(w1c21R, 50, 450);
    ws.placeImageXY(w1c22R, 150, 450);
    ws.placeImageXY(w1c23R, 250, 450);
    ws.placeImageXY(w1c24R, 350, 450);
    ws.placeImageXY(w1c25R, 450, 450);

    return ws;

  }

  // example cell images
  RectangleImage c1IMG = new RectangleImage(50, 50, "solid", Color.pink);
  RectangleImage c2IMG = new RectangleImage(50, 50, "solid", Color.cyan);

  // example flood it worlds
  FloodItWorld world1 = new FloodItWorld(3);
  FloodItWorld world2 = new FloodItWorld(10, 5);

  // example lists of colors
  ArrayList<Color> world1Cols = new ArrayList<Color>(Arrays.asList(Color.red, Color.blue,
      Color.magenta, Color.orange, Color.cyan, Color.pink, Color.green, Color.yellow));

  ArrayList<Color> world2Cols = new ArrayList<Color>(
      Arrays.asList(Color.red, Color.blue, Color.magenta));

  // tests for gen game colors
  void testGenGameColors(Tester t) {
    this.initData();
    t.checkExpect(this.world1.gameColors, this.world1Cols);
    t.checkExpect(this.world2.gameColors, this.world2Cols);
  }

  // tests for render cell
  void testRenderCell(Tester t) {
    this.initData();
    t.checkExpect(this.w1c1.renderCell(50), this.c1IMG);
    t.checkExpect(this.w1c2.renderCell(50), this.c2IMG);
  }

  // tests for assign
  void testAssign(Tester t) {
    this.initData();
    this.w1c1.assign(this.world1Board);
    t.checkExpect(this.w1c1.left, this.empty);
    t.checkExpect(this.w1c1.right, this.w1c2);
    t.checkExpect(this.w1c1.bottom, this.w1c6);
  }

  // tests for init board
  void testInitBoard(Tester t) {
    this.initData();
    t.checkExpect(this.world1.board, this.world1Board);
  }

  void testMakeScene(Tester t) {
    this.initData();
    t.checkExpect(this.world1.makeScene(), this.sceneMaker());
  }

  // tests for big bang
  void testBigBang(Tester t) {
    this.initData();
    this.world2.bigBang(500, 500, 0.1);
  }

  // tests for same cell
  void testSameCell(Tester t) {
    this.initData();
    t.checkExpect(this.w1c1.sameCell(new Posn(300, 250), 100, 0, 0), false);
    t.checkExpect(this.w1c1.sameCell(new Posn(50, 50), 100, 0, 0), true);
    t.checkExpect(this.w1c2.sameCell(new Posn(150, 50), 100, 1, 0), true);
  }

  // tests for find cell
  void testFindCell(Tester t) {
    this.initData();
    t.checkExpect(this.world1.findCell(new Posn(50, 50)), this.w1c1);
    t.checkExpect(this.world1.findCell(new Posn(150, 50)), this.w1c2);
  }

  // tests for same color
  void testSameColor(Tester t) {
    this.initData();
    t.checkExpect(this.w1c1.sameColor(this.w1c11), true);
    t.checkExpect(this.w1c1.sameColor(this.w1c2), false);
    t.checkExpect(this.w1c1.sameColor(new MtCell()), false);
  }

  // tests for on mouse clicked
  void testOnMousePressed(Tester t) {
    this.initData();
    this.world1.onMousePressed(new Posn(150, 50));
    t.checkExpect(this.w1c1.color, Color.pink);
    this.world1.onMousePressed(new Posn(450, 50));
    t.checkExpect(this.w1c1.color, Color.pink);
    this.world1.onMousePressed(new Posn(350, 50));
    t.checkExpect(this.w1c3.color, Color.red);

  }

  // tests for is flooded
  void testIsFlooded(Tester t) {
    this.initData();
    t.checkExpect(this.world1.isFlooded(), false);
  }

  // tests for update
  void testUpdate(Tester t) {
    this.initData();
    this.w1c1.update(this.w1c3);
    t.checkExpect(this.w1c1.color, Color.red);
    this.initData();
    this.w1c1.update(this.w1c22);
    t.checkExpect(this.w1c1.color, Color.pink);
  }

  // tests for last scene
  void testLastScene(Tester t) {
    this.initData();
    this.world1.clicks = 4;
    this.world1.tries = 3;
    WorldScene background = this.world1.getEmptyScene();
    WorldScene background2 = this.world1.getEmptyScene();
    TextImage msg = new TextImage("LOSER! :(", Color.red);
    TextImage msg2 = new TextImage("WINNER!! :D", Color.green);
    background.placeImageXY(msg, 500 / 2, 500 / 2);
    background2.placeImageXY(msg2, 500 / 2, 500 / 2);
    t.checkExpect(this.world1.lastScene("LOSER! :("), background);
    this.initData();
    t.checkExpect(this.world1.lastScene("WINNER!! :D"), background2);
  }
}