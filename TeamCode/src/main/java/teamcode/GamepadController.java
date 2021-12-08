package teamcode;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.Hashtable;

/**
 * Class that acts as a wrapper for the gamepad to make getting the keydown, keyup, etc state easier
 */
public class GamepadController {

    private static final String TAG = "vuf.test.gpcontroller";

    private final static ToggleButton[] toggleButtonList = ToggleButton.values();

    private final Gamepad gamepad;
    // dict of the controller states, as in keydown, hold, up, etc
    private final Hashtable<ToggleButton, ButtonState> controllerToggleStates;
    // dict of the actual value of the gamepad, true, false
    private final Hashtable<ToggleButton, Boolean> gamepadToggleValues;
    // dict of float button values, accessed directly (no enum or anything) since theyre just values
    private final Hashtable<FloatButton, Float> gamepadFloatValues;



    /**
     * Initializes the gamepad controller and sets initial values for the controller states
     * @param gamepad The gamepad to wrap with state functionality
     */
    public GamepadController(Gamepad gamepad) {
        this.gamepad = gamepad;

        // initialize dicts
        controllerToggleStates = new Hashtable<ToggleButton, ButtonState>();
        gamepadToggleValues = new Hashtable<ToggleButton, Boolean>();
        gamepadFloatValues = new Hashtable<FloatButton, Float>();
        initializeGamepadStates();
    }

    /**
     * Initializes the controller states with inactive or their float value for float buttons
     */
    public void initializeGamepadStates() {
        // set button values
        updateButtonValues();

        // set initial state for all buttons to inactive
        for (ToggleButton toggleButton : ToggleButton.values()) {
            controllerToggleStates.put(toggleButton, ButtonState.KEY_INACTIVE);
        }

        updateButtonStates();
    }

    /**
     * Updates all button values and controller states
     * MUST BE CALLED IN TELEOP LOOP FOR BUTTONS TO UPDATE
     */
    public void updateButtonStates() {
        updateButtonValues();
        updateToggleButtonStates();
    }

    /**
     * Updates controller toggle button states
     */
    public void updateToggleButtonStates() {
        /*
        Update state values
		Essentially contains transitions between button states, and hold down/hold up
         if controller state is inactive and button is on, state is down since it was up before but is now down
         if controller state is down and button is on, state is hold since nothing changed and button is still down
         if state is down/hold and button is up, state is up since it was down before but is now up
         if state is up and button is up, state is inactive since nothing changed and button is still up
         */
        for (ToggleButton toggleButton : toggleButtonList) {
            boolean buttonVal = gamepadToggleValues.get(toggleButton);
            switch (controllerToggleStates.get(toggleButton)) {
                case KEY_INACTIVE:
                    if (buttonVal) {
                        controllerToggleStates.put(toggleButton, ButtonState.KEY_DOWN);
                    }
                    break;
                case KEY_DOWN:
                    if (buttonVal) {
                        controllerToggleStates.put(toggleButton, ButtonState.KEY_HOLD);
                    } else {
                        controllerToggleStates.put(toggleButton, ButtonState.KEY_UP);
                    }
                    break;
                case KEY_HOLD:
                    if (!buttonVal) {
                        controllerToggleStates.put(toggleButton, ButtonState.KEY_UP);
                    }
                    break;
                case KEY_UP:
                    if (buttonVal) {
                        controllerToggleStates.put(toggleButton, ButtonState.KEY_DOWN);
                    } else {
                        controllerToggleStates.put(toggleButton, ButtonState.KEY_INACTIVE);
                    }
                    break;
            }
        }
    }



    /**
     * Updates button values for toggle and float buttons
     * Probably has to be manual because primitives dont point to memory (pretty sure)
     *
     * NOTE: to treat a float button as an on/off button, add (value > 0) to the toggle value dict
     * left/right trigger are currently floats treated as booleans
     *
     * no back button because it exits the app lmao
     */
    public void updateButtonValues() {
        // update toggle values
        gamepadToggleValues.put(ToggleButton.A, gamepad.a);
        gamepadToggleValues.put(ToggleButton.B, gamepad.b);
        gamepadToggleValues.put(ToggleButton.X, gamepad.x);
        gamepadToggleValues.put(ToggleButton.Y, gamepad.y);
        gamepadToggleValues.put(ToggleButton.DPAD_UP, gamepad.dpad_up);
        gamepadToggleValues.put(ToggleButton.DPAD_DOWN, gamepad.dpad_down);
        gamepadToggleValues.put(ToggleButton.DPAD_LEFT, gamepad.dpad_left);
        gamepadToggleValues.put(ToggleButton.DPAD_RIGHT, gamepad.dpad_right);
        gamepadToggleValues.put(ToggleButton.LEFT_BUMPER, gamepad.left_bumper);
        gamepadToggleValues.put(ToggleButton.RIGHT_BUMPER, gamepad.right_bumper);
        gamepadToggleValues.put(ToggleButton.LEFT_STICK_BUTTON, gamepad.left_stick_button);
        gamepadToggleValues.put(ToggleButton.RIGHT_STICK_BUTTON, gamepad.right_stick_button);
        gamepadToggleValues.put(ToggleButton.START_BUTTON, gamepad.start);
        gamepadToggleValues.put(ToggleButton.CENTER_BUTTON, gamepad.guide);
        gamepadToggleValues.put(ToggleButton.BACK_BUTTON, gamepad.back);


        gamepadToggleValues.put(ToggleButton.LEFT_TRIGGER, (gamepad.left_trigger > 0)); // ACTUALLY A FLOAT
        gamepadToggleValues.put(ToggleButton.RIGHT_TRIGGER, (gamepad.right_trigger > 0)); // ^

        // update float values
        gamepadFloatValues.put(FloatButton.LEFT_STICK_X, gamepad.left_stick_x);
        gamepadFloatValues.put(FloatButton.LEFT_STICK_Y, gamepad.left_stick_y);
        gamepadFloatValues.put(FloatButton.RIGHT_STICK_X, gamepad.right_stick_x);
        gamepadFloatValues.put(FloatButton.RIGHT_STICK_Y, gamepad.right_stick_y);
    }

    /**
     * Gets the state of a toggle button
     * @param button the button to get the state of
     * @return the state of the button, a ButtonState value
     */
    public ButtonState getButtonState(ToggleButton button) {
        return controllerToggleStates.get(button);
    }

    /**
     * Gets the value of a float button
     * @param button the button to get the state of
     * @return the float value of the button
     */
    public float getButtonState(FloatButton button) {
        return gamepadFloatValues.get(button);
    }

    // enum of button states that are either on or off
    public enum ToggleButton {
        A,
        B,
        X,
        Y,
        DPAD_UP,
        DPAD_DOWN,
        DPAD_LEFT,
        DPAD_RIGHT,
        LEFT_BUMPER,
        RIGHT_BUMPER,
        LEFT_STICK_BUTTON,
        RIGHT_STICK_BUTTON,
        START_BUTTON,
        LEFT_TRIGGER, // THIS IS ACTUALLY A FLOAT
        RIGHT_TRIGGER, // ^
        BACK_BUTTON,
        CENTER_BUTTON,
    }

    // enum of buttons that have floats as values rather than booleans
    public enum FloatButton {
        LEFT_STICK_X, // FIXME: 8/19/21 right stick doesnt work correctly, idk why
        LEFT_STICK_Y,
        RIGHT_STICK_X,
        RIGHT_STICK_Y,
    }

    // enum of buttons states
    public enum ButtonState {
        KEY_DOWN,
        KEY_UP,
        KEY_HOLD,
        KEY_INACTIVE
    }
}
