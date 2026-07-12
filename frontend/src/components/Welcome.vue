<template>
    <div class="cardbox">
        <h1>SuperKL的服装管理后台欢迎您！</h1>
    </div>
</template>
<script setup>

</script>
<style scoped>
.cardbox {
    margin: auto;
    height: 100%;
    text-align: center;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
}

.cardbox h1 {
    margin-bottom: 20px;
    font-size: 2.5rem;
    font-weight: bold;
    background: linear-gradient(to right, #ff6b6b, #ffa726, #ffff00);
    background-size: 400% 100%;
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    color: transparent;
    animation: gradientText 3s ease infinite;
    transition: transform 0.3s ease;
    text-shadow: none;
}

.cardbox h1:hover{
    transform: scale(1.1);
}

@keyframes gradientText {
    0% {
        background-position: 0% 50%;
    }

    50% {
        background-position: 100% 50%;
    }

    100% {
        background-position: 0% 50%;
    }
}
</style>