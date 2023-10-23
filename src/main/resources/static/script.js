document.addEventListener('DOMContentLoaded', function () {

    function createRelation(rName, rSize) {
        // Define the URL of the API endpoint
        const apiUrl = 'http://localhost:8080/relations';

        // Data to send in the request body
        const data = {
            relationName: rName,
            relationSize: rSize,
            // attributeName: aName,
            // attributeSize: aSize,
        };

        // Make a POST request using the fetch API
        fetch(apiUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
        })
        .then((response) => {
            if (response.ok) {
            return response.text(); // Successful POST
            } else {
            throw new Error('Failed to add object.');
            }
        })
        .then((responseText) => {
            console.log(responseText); // Log the response from the server
        })
        .catch((error) => {
            console.error(error);
        });
    }

    
    const relationNameInput = document.getElementById('relationName');
    const relationSizeInput = document.getElementById('relationSize');
    const displayButton = document.getElementById('displayBtn');
    const tableDisplay = document.getElementById('tableDisplay');

    // Function to generate a random color with sufficient contrast for text
    function getRandomColor() {
        const contrastThreshold = 128; // Threshold for text color contrast
        const color = `rgb(${Math.random() * 256}, ${Math.random() * 256}, ${Math.random() * 256})`;
        const textColor = colorBrightness(color) < contrastThreshold ? 'white' : 'black';
        return { background: color, text: textColor };
    }

    // Function to calculate color brightness
    function colorBrightness(color) {
        const [r, g, b] = color.match(/\d+/g).map(Number);
        return (r * 299 + g * 587 + b * 114) / 1000;
    }

    relationNameInput.addEventListener('input', function () {
        if (relationNameInput.value.trim() !== '') {
            relationSizeInput.removeAttribute('disabled');
        } else {
            relationSizeInput.setAttribute('disabled', 'disabled');
            displayButton.setAttribute('disabled', 'disabled');
        }
    });

    relationSizeInput.addEventListener('input', function () {
        const relationName = relationNameInput.value;
        const relationSize = parseInt(relationSizeInput.value);
        if (relationName.trim() !== '' && !isNaN(relationSize)) {
            displayButton.removeAttribute('disabled');
        } else {
            displayButton.setAttribute('disabled', 'disabled');
        }
    });

    displayButton.addEventListener('click', function () {
        const relationName = relationNameInput.value;
        const relationSize = parseInt(relationSizeInput.value);

        if (relationName.trim() !== '' && !isNaN(relationSize)) {
            // Create a new table box with a random background and text color
            const randomColors = getRandomColor();
            const newTableBox = document.createElement('div');
            newTableBox.classList.add('table-box');
            newTableBox.style.backgroundColor = randomColors.background;
            newTableBox.style.color = randomColors.text;
            newTableBox.innerHTML = `<div class="displayed-name">${relationName}</div><div class="displayed-size">Table Size: ${relationSize} rows</div>`;
            createRelation(relationName, relationSize)


            // Remove the first box if there are more than 4 boxes
            const tableBoxes = document.querySelectorAll('.table-box');
            if (tableBoxes.length > 3) {
                tableDisplay.removeChild(tableBoxes[0]);
            }

            // Append the new table box to the tableDisplay
            tableDisplay.appendChild(newTableBox);

            // Reset the inputs and disable the second textbox
            relationNameInput.value = '';
            relationSizeInput.value = '';
            relationSizeInput.setAttribute('disabled', 'disabled');
            displayButton.setAttribute('disabled', 'disabled');
        }
    });
    
});
