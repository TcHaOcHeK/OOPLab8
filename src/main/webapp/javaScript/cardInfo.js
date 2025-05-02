
document.addEventListener('DOMContentLoaded', function() {
    loadEquipment();

    document.getElementById('equipmentForm').addEventListener('submit', function(e) {
        e.preventDefault();
        addEquipment();
    });
});

function loadEquipment() {
    fetch('equipment')
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка сети');
            }
            return response.json();
        })
        .then(data => {
            displayEquipment(data);
        })
        .catch(error => {
            showMessage(error.message, 'error');
        });
}

function addEquipment() {
    const equipment = {
        name: document.getElementById('name').value,
        type: document.getElementById('type').value,
        brand: document.getElementById('brand').value,
        model: document.getElementById('model').value,
        year: document.getElementById('year').value,
        price: document.getElementById('price').value,
        condition: document.getElementById('condition').value
    };

    fetch('equipment', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(equipment)
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => { throw new Error(err.message); });
            }
            return response.json();
        })
        .then(data => {
            showMessage('Оборудование успешно добавлено', 'success');
            document.getElementById('equipmentForm').reset();
            loadEquipment();
        })
        .catch(error => {
            showMessage(error.message, 'error');
        });
}

function displayEquipment(equipmentList) {
    const container = document.getElementById('equipmentList');
    container.innerHTML = '';

    if (!equipmentList || equipmentList.length === 0) {
        container.innerHTML = '<p>Нет оборудования в коллекции. Добавьте первое!</p>';
        return;
    }

    equipmentList.forEach(equipment => {
        const card = document.createElement('div');
        card.className = 'equipment-card';

        card.innerHTML = `
                    <h3>${equipment.name}</h3>
                    <div class="property"><span class="property-name">Тип:</span> ${equipment.type}</div>
                    <div class="property"><span class="property-name">Бренд/Модель:</span> ${equipment.brand} ${equipment.model}</div>
                    <div class="property"><span class="property-name">Год выпуска:</span> ${equipment.year}</div>
                    <div class="property"><span class="property-name">Цена:</span> ${equipment.price} руб</div>
                    <div class="property"><span class="property-name">Состояние:</span> ${equipment.condition}</div>
                `;

        container.appendChild(card);
    });
}

function showMessage(text, type) {
    const messageDiv = document.getElementById('message');
    messageDiv.textContent = text;
    messageDiv.className = type;

    setTimeout(() => {
        messageDiv.textContent = '';
        messageDiv.className = '';
    }, 5000);
}