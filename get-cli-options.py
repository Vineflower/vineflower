import os
import re

# Constant pattern for matching Java comments
JAVA_COMMENT_PATTERN = r'//.*$'

def extract_console_help(filepath):
    """Extracts default help information from a given Java file.
    
    Parameters:
        filepath (str): The path to the Java file.
    
    Returns:
        list: A list of lines containing the default help information.
    """
    # Read all lines from the file
    with open(filepath, 'r') as f:
        lines = f.readlines()

    default_help = []
    inside_default_help = False  # Flag to indicate if we are inside the relevant Java array

    # Loop through each line in the file
    for line in lines:
        # Start capturing lines when the DEFAULT_HELP array starts
        if "private static final String[] DEFAULT_HELP = {" in line:
            inside_default_help = True
            continue
        # Stop capturing when the array ends
        if inside_default_help and "};" in line:
            inside_default_help = False

        # Process lines that are inside the DEFAULT_HELP array
        if inside_default_help:
            # Remove Java comments
            clean_line = re.sub(JAVA_COMMENT_PATTERN, '', line)
            # Remove extra characters
            clean_line = clean_line.strip(" \",\n")

            # Check for lines that contain empty strings and treat them as new lines
            if '""' in line:
                default_help.append("") # This will be a new line when joined
            # Add non-empty lines to the list
            elif clean_line:
                default_help.append(clean_line)

    return default_help

def extract_fernflower_preferences(filepath, option_types):
    """Extracts Fernflower preferences from a given Java file.
    
    Parameters:
        filepath (str): The path to the Java file.
        option_types (dict): A dictionary mapping option names to their types.
        
    Returns:
        list: A list of strings detailing each preference.
    """
    # Read all lines from the file
    with open(filepath, 'r') as f:
        lines = f.readlines()

    preferences = []

    # Loop through each line in the file
    for idx, line in enumerate(lines):
        # Look for lines that have the "@Name" annotation
        if "@Name" in line:
            try:
                # Extract the name and description using regex
                name_line = re.search(r'@Name\("(.+)"\)', line.strip()).group(1)
                description_line = re.search(r'@Description\("(.+)"\)', lines[idx+1].strip()).group(1)
                # Extract the option name
                option_name = re.search(r'String (\w+) = "(\w+)"', lines[idx+2].strip()).group(2)
                # Get the type of the option from the provided dictionary
                type_hint = option_types.get(option_name, 'unknown')
                # Add the formatted string to the list
                preferences.append(f"-{option_name}=<{type_hint}> - {name_line}: {description_line} (default: true)")
            except AttributeError:
                print(f"Failed to extract data at line {idx}")

    return preferences

def search_and_extract(base_directory, option_types):
    """Searches for specific Java files in a directory and extracts relevant information.
    
    Parameters:
        base_directory (str): The directory to search in.
        option_types (dict): A dictionary mapping option names to their types.
        
    Returns:
        tuple: A tuple containing two lists; one for default help and one for preferences.
    """
    # Initialize empty lists for storing information
    default_help = []
    fernflower_preferences = []
    
    # Walk through the directory structure
    for root, _, filenames in os.walk(base_directory):
        # Check if ConsoleHelp.java exists and extract default help
        if "ConsoleHelp.java" in filenames:
            default_help = extract_console_help(os.path.join(root, "ConsoleHelp.java"))
        
        # Check if IFernflowerPreferences.java exists and extract preferences
        if "IFernflowerPreferences.java" in filenames:
            fernflower_preferences = extract_fernflower_preferences(os.path.join(root, "IFernflowerPreferences.java"), option_types)

    return default_help, fernflower_preferences

if __name__ == "__main__":

    # Dictionary to hold option types
    option_types = { #There is probably a better way to do this
        'rbr':'bool', 'rsy':'bool', 'din':'bool', 'dc4':'bool', 'das':'bool',
        'hes':'bool', 'hdc':'bool', 'dgs':'bool', 'ner':'bool', 'esm':'bool',
        'den':'bool', 'dpr':'bool', 'rgn':'bool', 'lit':'bool', 'bto':'bool',
        'asc':'bool', 'nns':'bool', 'uto':'bool', 'udv':'bool', 'ump':'bool',
        'rer':'bool', 'fdi':'bool', 'inn':'bool', 'lac':'bool', 'bsm':'bool',
        'dcl':'bool', 'iib':'bool', 'vac':'bool', 'tcs':'bool', 'pam':'bool',
        'tlf':'bool', 'tco':'bool', 'swe':'bool', 'shs':'bool', 'ovr':'bool',
        'ssp':'bool', 'vvm':'bool', 'iec':'bool', 'jrt':'string', 'ega':'bool',
        'isl':'bool', 'log':'string', 'mpm':'bool', 'ren':'bool', 'urc':'string',
        'nls':'bool', 'ind':'string', 'pll':'int', 'ban':'string', 'erm':'string',
        'thr':'int', 'jvn':'bool', 'jpr':'bool', 'sef':'bool', 'win':'bool', 'dbe':'bool',
        'dee':'bool', 'dec':'bool', 'sfc':'bool', 'dcc':'bool', 'fji':'bool'
    }

    # Specify directory to be scanned
    base_directory = "./"  # Replace this if needed
    # Run the extraction
    default_help, preferences = search_and_extract(base_directory, option_types)

    # Print the extracted information
    print("\n".join(default_help))

    for preference in preferences:
        print(preference)